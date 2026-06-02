package com.rhacarys.racharcontas.domain.service;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rhacarys.racharcontas.api.dto.ExpenseResponse;
import com.rhacarys.racharcontas.api.dto.MembershipResponse;
import com.rhacarys.racharcontas.api.dto.PartyResponse;
import com.rhacarys.racharcontas.api.dto.SyncPushRequest;
import com.rhacarys.racharcontas.api.dto.SyncResponse;
import com.rhacarys.racharcontas.domain.exception.BusinessException;
import com.rhacarys.racharcontas.domain.model.Expense;
import com.rhacarys.racharcontas.domain.model.ExpenseSplit;
import com.rhacarys.racharcontas.domain.model.ExpenseType;
import com.rhacarys.racharcontas.domain.model.Membership;
import com.rhacarys.racharcontas.domain.model.User;
import com.rhacarys.racharcontas.domain.repository.ExpenseRepository;
import com.rhacarys.racharcontas.domain.repository.MembershipRepository;
import com.rhacarys.racharcontas.domain.repository.PartyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class SyncService {

    private final PartyRepository partyRepository;
    private final MembershipRepository membershipRepository;
    private final ExpenseRepository expenseRepository;
    private final ExpenseService expenseService;

    @Transactional(readOnly = true)
    public SyncResponse getUpdatesSince(UUID partyId, Instant lastSync, User loggedUser) {
        log.debug("Generating delta sync for partyId: {} since: {}", partyId, lastSync);

        if (!membershipRepository.existsByPartyIdAndUserId(partyId, loggedUser.getId())) {
            throw new BusinessException("Acesso negado ou vínculo removido.", HttpStatus.FORBIDDEN);
        }

        PartyResponse partyResponse = partyRepository.findModifiedSince(partyId, lastSync)
                .map(party -> PartyResponse.fromEntity(party, null))
                .orElse(null);

        List<MembershipResponse> memberships = membershipRepository.findModifiedSince(partyId, lastSync).stream()
                .map(MembershipResponse::fromEntity)
                .toList();

        List<ExpenseResponse> expenses = expenseRepository.findModifiedSince(partyId, lastSync).stream()
                .map(ExpenseResponse::fromEntity)
                .toList();

        return new SyncResponse(partyResponse, memberships, expenses, Instant.now());
    }

    @Transactional
    public void processBatchSync(UUID partyId, SyncPushRequest request, User loggedUser) {
        log.info("Processando sincronização em lote (Push) - partyId: {}, userId: {}", partyId, loggedUser.getId());

        if (request.expensesToDelete() != null) {
            request.expensesToDelete().forEach(expenseId -> {
                try {
                    expenseService.deleteExpense(partyId, expenseId, loggedUser);
                } catch (Exception e) {
                    log.warn("Ignorando falha ao deletar despesa no sync (provavelmente já excluída): {}", expenseId);
                }
            });
        }

        if (request.expensesToCreate() != null) {
            request.expensesToCreate().forEach(payload -> {
                try {
                    insertExpense(partyId, payload);
                } catch (Exception e) {
                    log.warn("Falha ao salvar despesa {} durante o sync: {}", payload.id(), e.getMessage());
                }
            });
        }
    }

    private void insertExpense(UUID partyId, SyncPushRequest.ExpenseSyncPayload payload) {
        if (expenseRepository.existsById(payload.id())) {
            log.warn("Despesa {} já existe. Edições não são permitidas na regra de negócio.", payload.id());
            return;
        }

        Membership payer = membershipRepository.findById(payload.payerId())
                .filter(m -> m.getParty().getId().equals(partyId) && m.getDeletedAt() == null)
                .orElseThrow(() -> new BusinessException("Pagador inválido ou inativo."));

        Expense expense = new Expense();
        expense.setId(payload.id());
        expense.setParty(payer.getParty());
        expense.setPayer(payer);
        expense.setDescription(payload.description());
        expense.setAmount(payload.amount());
        expense.setDate(payload.date());
        expense.setType(payload.type() != null ? payload.type() : ExpenseType.PURCHASE);

        for (var splitReq : payload.splits()) {
            Membership debtor = membershipRepository.findById(splitReq.debtorId())
                    .orElseThrow(() -> new BusinessException("Devedor não encontrado."));

            ExpenseSplit split = new ExpenseSplit();
            split.setDebtor(debtor);
            split.setAmount(splitReq.amount());
            expense.addSplit(split);
        }

        expenseRepository.save(expense);
    }
}