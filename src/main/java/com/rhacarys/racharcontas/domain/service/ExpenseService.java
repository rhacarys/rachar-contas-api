package com.rhacarys.racharcontas.domain.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rhacarys.racharcontas.api.dto.ExpenseRequest;
import com.rhacarys.racharcontas.api.dto.ExpenseResponse;
import com.rhacarys.racharcontas.domain.event.ExpenseCreatedEvent;
import com.rhacarys.racharcontas.domain.exception.BusinessException;
import com.rhacarys.racharcontas.domain.model.Expense;
import com.rhacarys.racharcontas.domain.model.ExpenseSplit;
import com.rhacarys.racharcontas.domain.model.ExpenseType;
import com.rhacarys.racharcontas.domain.model.Membership;
import com.rhacarys.racharcontas.domain.model.Party;
import com.rhacarys.racharcontas.domain.model.User;
import com.rhacarys.racharcontas.domain.repository.ExpenseRepository;
import com.rhacarys.racharcontas.domain.repository.MembershipRepository;
import com.rhacarys.racharcontas.domain.repository.PartyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final PartyRepository partyRepository;
    private final MembershipRepository membershipRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public ExpenseResponse createExpense(UUID partyId, ExpenseRequest request, User loggedUser) {
        log.debug("Creating expense for partyId: {}, userId: {}, amount: {}",
                partyId, loggedUser.getId(), request.amount());

        validateExpenseMath(request);
        validateUserInParty(partyId, loggedUser.getId());

        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new BusinessException("Party not found", HttpStatus.NOT_FOUND));

        Membership payer = getPayer(request.payerId(), partyId);
        Map<UUID, Membership> debtorsMap = getDebtorsMap(request, partyId);

        Expense expense = buildExpenseEntity(request, party, payer, debtorsMap);
        Expense savedExpense = expenseRepository.save(expense);

        log.info("Expense created - expenseId: {}, partyId: {}, payerId: {}, amount: {}",
                savedExpense.getId(), partyId, payer.getId(), request.amount());

        publishExpenseCreatedEvent(savedExpense);

        return ExpenseResponse.fromEntity(savedExpense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> getPartyExpenses(UUID partyId, User loggedUser) {
        log.debug("Fetching expenses for partyId: {}, userId: {}", partyId, loggedUser.getId());

        validateUserInParty(partyId, loggedUser.getId());

        List<ExpenseResponse> expenses = expenseRepository.findByPartyIdOrderByDateDesc(partyId).stream()
                .map(ExpenseResponse::fromEntity)
                .toList();

        log.debug("Found {} expenses for partyId: {}", expenses.size(), partyId);
        return expenses;
    }

    @Transactional
    public void deleteExpense(UUID partyId, UUID expenseId, User loggedUser) {
        log.debug("Deleting expense - expenseId: {}, partyId: {}, userId: {}",
                expenseId, partyId, loggedUser.getId());

        Expense expense = expenseRepository.findById(expenseId)
                .filter(e -> e.getParty().getId().equals(partyId))
                .orElseThrow(() -> new BusinessException("Expense not found", HttpStatus.NOT_FOUND));

        Membership loggedUserMembership = membershipRepository.findByPartyIdAndUserId(partyId, loggedUser.getId())
                .orElseThrow(() -> new BusinessException("You are not a member of this party", HttpStatus.FORBIDDEN));

        boolean isPayer = expense.getPayer().getUser().getId().equals(loggedUser.getId());
        boolean isAdmin = "ADMIN".equals(loggedUserMembership.getRole());

        if (!isPayer && !isAdmin) {
            log.warn("Expense deletion denied - userId: {}, expenseId: {}, not payer or admin",
                    loggedUser.getId(), expenseId);
            throw new BusinessException("Only the payer or an ADMIN can delete this expense", HttpStatus.FORBIDDEN);
        }

        expense.setDeletedAt(Instant.now());
        expenseRepository.save(expense);

        log.info("Expense soft-deleted - expenseId: {}, partyId: {}, userId: {}",
                expenseId, partyId, loggedUser.getId());
    }

    @Transactional
    public void deleteExpensesBatch(UUID partyId, List<UUID> expenseIds, User loggedUser) {
        if (expenseIds == null || expenseIds.isEmpty())
            return;

        Membership userMembership = membershipRepository.findByPartyIdAndUserId(partyId, loggedUser.getId())
                .orElseThrow(() -> new BusinessException("Acesso negado.", HttpStatus.FORBIDDEN));

        boolean isAdmin = "ADMIN".equals(userMembership.getRole());

        List<Expense> requestedExpenses = expenseRepository.findAllById(expenseIds).stream()
                .filter(e -> e.getParty().getId().equals(partyId))
                .toList();

        List<Expense> allowedToDelete = requestedExpenses.stream()
                .filter(e -> isAdmin || e.getPayer().getUser().getId().equals(loggedUser.getId()))
                .toList();

        if (!allowedToDelete.isEmpty()) {
            allowedToDelete.forEach(e -> e.setDeletedAt(Instant.now()));
            expenseRepository.saveAll(allowedToDelete);

            log.info("Batch delete executado - partyId: {}, userId: {}, excluídas: {}/{}",
                    partyId, loggedUser.getId(), allowedToDelete.size(), requestedExpenses.size());
        }
    }

    private void validateExpenseMath(ExpenseRequest request) {
        BigDecimal totalSplits = request.splits().stream()
                .map(ExpenseRequest.SplitRequest::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSplits.compareTo(request.amount()) != 0) {
            log.warn("Expense validation failed - total: {}, splits total: {}",
                    request.amount(), totalSplits);
            throw new BusinessException("The sum of splits must equal the total expense amount",
                    HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private void validateUserInParty(UUID partyId, UUID userId) {
        if (!membershipRepository.existsByPartyIdAndUserId(partyId, userId)) {
            log.warn("User not a member of party - partyId: {}, userId: {}", partyId, userId);
            throw new BusinessException("You are not a member of this party", HttpStatus.FORBIDDEN);
        }
    }

    private Membership getPayer(UUID payerId, UUID partyId) {
        return membershipRepository.findById(payerId)
                .filter(m -> m.getParty().getId().equals(partyId))
                .orElseThrow(() -> new BusinessException("Payer membership not found", HttpStatus.BAD_REQUEST));
    }

    private Map<UUID, Membership> getDebtorsMap(ExpenseRequest request, UUID partyId) {
        List<UUID> debtorIds = request.splits().stream()
                .map(ExpenseRequest.SplitRequest::debtorId)
                .toList();

        List<Membership> debtors = membershipRepository.findAllByIdInAndPartyId(debtorIds, partyId);

        if (debtors.size() != debtorIds.size()) {
            log.warn("Debtors validation failed - requested: {}, found: {}", debtorIds.size(), debtors.size());
            throw new BusinessException("One or more debtors do not belong to this party", HttpStatus.BAD_REQUEST);
        }

        return debtors.stream().collect(Collectors.toMap(Membership::getId, m -> m));
    }

    private Expense buildExpenseEntity(ExpenseRequest request, Party party, Membership payer,
            Map<UUID, Membership> debtorsMap) {
        Expense expense = new Expense();
        expense.setParty(party);
        expense.setPayer(payer);
        expense.setType(request.type() != null ? request.type() : ExpenseType.PURCHASE);
        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setDate(request.date());

        for (var splitReq : request.splits()) {
            ExpenseSplit split = new ExpenseSplit();
            split.setDebtor(debtorsMap.get(splitReq.debtorId()));
            split.setAmount(splitReq.amount());
            expense.addSplit(split);
        }
        return expense;
    }

    private void publishExpenseCreatedEvent(Expense expense) {
        List<UUID> debtorIds = expense.getSplits().stream()
                .map(split -> split.getDebtor().getId())
                .toList();

        ExpenseCreatedEvent event = new ExpenseCreatedEvent(
                expense.getId(),
                expense.getParty().getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getPayer().getAlias(),
                debtorIds);

        log.debug("Publishing application event for new expense: {}", expense.getId());
        eventPublisher.publishEvent(event);
    }
}