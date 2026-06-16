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

    public void processBatchSync(UUID partyId, SyncPushRequest request, User loggedUser) {
        log.info("Processando sincronização em lote (Push) - partyId: {}, userId: {}", partyId, loggedUser.getId());

        if (request.expensesToDelete() != null && !request.expensesToDelete().isEmpty()) {
            expenseService.deleteExpensesBatch(partyId, request.expensesToDelete(), loggedUser);
        }

        if (request.expensesToCreate() != null) {
            request.expensesToCreate().forEach(payload -> {
                try {
                    expenseService.insertExpenseTransactional(partyId, payload);
                } catch (Exception e) {
                    log.warn("Falha ao salvar despesa {} durante o sync: {}", payload.id(), e.getMessage());
                }
            });
        }
    }
}