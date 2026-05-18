package com.rhacarys.contaconjunta.domain.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rhacarys.contaconjunta.api.dto.PartyBalanceResponse;
import com.rhacarys.contaconjunta.api.dto.PartyBalanceResponse.MemberBalance;
import com.rhacarys.contaconjunta.domain.exception.BusinessException;
import com.rhacarys.contaconjunta.domain.model.Expense;
import com.rhacarys.contaconjunta.domain.model.ExpenseSplit;
import com.rhacarys.contaconjunta.domain.model.Membership;
import com.rhacarys.contaconjunta.domain.model.User;
import com.rhacarys.contaconjunta.domain.repository.ExpenseRepository;
import com.rhacarys.contaconjunta.domain.repository.MembershipRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BalanceService {

    private final MembershipRepository membershipRepository;
    private final ExpenseRepository expenseRepository;

    /**
     * Calculates the balance for each member in a party based on all expenses and
     * splits.
     * Positive balance = user is owed money, negative = user owes money.
     */
    @Transactional(readOnly = true)
    public PartyBalanceResponse calculateBalances(UUID partyId, User user) {
        log.debug("Calculating balances for partyId: {}, userId: {}", partyId, user.getId());

        validateUserInParty(partyId, user.getId());

        List<Membership> members = membershipRepository.findByPartyId(partyId);
        List<Expense> expenses = expenseRepository.findAllByPartyIdWithSplits(partyId);
        Map<Membership, BigDecimal> balances = computeBalances(members, expenses);

        PartyBalanceResponse response = buildResponse(partyId, balances);
        log.debug("Balance calculation complete - partyId: {}, memberCount: {}, expenseCount: {}",
                partyId, members.size(), expenses.size());

        return response;
    }

    private void validateUserInParty(UUID partyId, UUID userId) {
        if (!membershipRepository.existsByPartyIdAndUserId(partyId, userId)) {
            log.warn("Balance calculation denied - userId: {}, not in partyId: {}", userId, partyId);
            throw new BusinessException("You are not a member of this party", HttpStatus.FORBIDDEN);
        }
    }

    /**
     * Iterates through all expenses and splits to compute member balances.
     * For each expense split: payer gains the amount (positive), debtor loses it
     * (negative).
     */
    private Map<Membership, BigDecimal> computeBalances(List<Membership> members, List<Expense> expenses) {
        Map<Membership, BigDecimal> balances = members.stream()
                .collect(Collectors.toMap(m -> m, m -> BigDecimal.ZERO));

        for (Expense expense : expenses) {
            Membership payer = expense.getPayer();

            for (ExpenseSplit split : expense.getSplits()) {
                Membership debtor = split.getDebtor();
                BigDecimal amount = split.getAmount();

                balances.merge(payer, amount, BigDecimal::add);
                balances.merge(debtor, amount.negate(), BigDecimal::add);
            }
        }
        return balances;
    }

    private PartyBalanceResponse buildResponse(UUID partyId, Map<Membership, BigDecimal> balances) {
        List<MemberBalance> memberBalances = balances.entrySet().stream()
                .map(entry -> new MemberBalance(
                        entry.getKey().getId(),
                        entry.getKey().getAlias(),
                        entry.getValue()))
                .sorted((b1, b2) -> b2.balance().compareTo(b1.balance()))
                .toList();

        return new PartyBalanceResponse(partyId, memberBalances);
    }
}