package com.rhacarys.contaconjunta.domain.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rhacarys.contaconjunta.api.dto.ExpenseRequest;
import com.rhacarys.contaconjunta.api.dto.ExpenseResponse;
import com.rhacarys.contaconjunta.domain.exception.BusinessException;
import com.rhacarys.contaconjunta.domain.model.Expense;
import com.rhacarys.contaconjunta.domain.model.ExpenseSplit;
import com.rhacarys.contaconjunta.domain.model.Membership;
import com.rhacarys.contaconjunta.domain.model.Party;
import com.rhacarys.contaconjunta.domain.model.User;
import com.rhacarys.contaconjunta.domain.repository.ExpenseRepository;
import com.rhacarys.contaconjunta.domain.repository.MembershipRepository;
import com.rhacarys.contaconjunta.domain.repository.PartyRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final PartyRepository partyRepository;
    private final MembershipRepository membershipRepository;

    @Transactional
    public ExpenseResponse createExpense(UUID partyId, ExpenseRequest request, User loggedUser) {
        validateExpenseMath(request);

        validateUserInParty(partyId, loggedUser.getId());

        Party party = partyRepository.findById(partyId)
                .orElseThrow(() -> new BusinessException("Party not found", HttpStatus.NOT_FOUND));
        
        Membership payer = getPayer(request.payerId(), partyId);
        Map<UUID, Membership> debtorsMap = getDebtorsMap(request, partyId);

        Expense expense = buildExpenseEntity(request, party, payer, debtorsMap);

        Expense savedExpense = expenseRepository.save(expense);
        return ExpenseResponse.fromEntity(savedExpense);
    }

    private void validateExpenseMath(ExpenseRequest request) {
        BigDecimal totalSplits = request.splits().stream()
                .map(ExpenseRequest.SplitRequest::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (totalSplits.compareTo(request.amount()) != 0) {
            throw new BusinessException("The sum of splits must equal the total expense amount", HttpStatus.UNPROCESSABLE_ENTITY);
        }
    }

    private void validateUserInParty(UUID partyId, UUID userId) {
        if (!membershipRepository.existsByPartyIdAndUserId(partyId, userId)) {
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
            throw new BusinessException("One or more debtors do not belong to this party", HttpStatus.BAD_REQUEST);
        }

        return debtors.stream().collect(Collectors.toMap(Membership::getId, m -> m));
    }

    private Expense buildExpenseEntity(ExpenseRequest request, Party party, Membership payer, Map<UUID, Membership> debtorsMap) {
        Expense expense = new Expense();
        expense.setParty(party);
        expense.setPayer(payer);
        expense.setDescription(request.description());
        expense.setAmount(request.amount());
        expense.setDate(request.date());

        for (var splitReq : request.splits()) {
            ExpenseSplit split = new ExpenseSplit();
            split.setDebtor(debtorsMap.get(splitReq.debtorId()));
            split.setAmount(splitReq.amount());
            split.setSettled(false);
            expense.addSplit(split);
        }
        return expense;
    }
}