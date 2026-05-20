package com.rhacarys.racharcontas.domain.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rhacarys.racharcontas.api.dto.PartyBalanceResponse;
import com.rhacarys.racharcontas.domain.exception.BusinessException;
import com.rhacarys.racharcontas.domain.model.Expense;
import com.rhacarys.racharcontas.domain.model.ExpenseSplit;
import com.rhacarys.racharcontas.domain.model.Membership;
import com.rhacarys.racharcontas.domain.model.User;
import com.rhacarys.racharcontas.domain.repository.ExpenseRepository;
import com.rhacarys.racharcontas.domain.repository.MembershipRepository;

@ExtendWith(MockitoExtension.class)
class BalanceServiceTest {

    @Mock
    private MembershipRepository membershipRepository;

    @Mock
    private ExpenseRepository expenseRepository;

    @InjectMocks
    private BalanceService balanceService;

    @Test
    void calculateBalances_ShouldComputeCorrectBalances_WhenDataIsValid() {
        UUID partyId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());

        Membership m1 = new Membership();
        m1.setId(UUID.randomUUID());
        m1.setAlias("Ana");
        Membership m2 = new Membership();
        m2.setId(UUID.randomUUID());
        m2.setAlias("Bia");

        Expense expense = new Expense();
        expense.setPayer(m1);

        ExpenseSplit s1 = new ExpenseSplit();
        s1.setDebtor(m1);
        s1.setAmount(new BigDecimal("15.00"));
        ExpenseSplit s2 = new ExpenseSplit();
        s2.setDebtor(m2);
        s2.setAmount(new BigDecimal("15.00"));
        expense.setSplits(List.of(s1, s2));

        when(membershipRepository.existsByPartyIdAndUserId(partyId, user.getId())).thenReturn(true);
        when(membershipRepository.findByPartyId(partyId)).thenReturn(List.of(m1, m2));
        when(expenseRepository.findAllByPartyIdWithSplits(partyId)).thenReturn(List.of(expense));

        PartyBalanceResponse response = balanceService.calculateBalances(partyId, user);

        assertEquals(2, response.balances().size());
        assertEquals(new BigDecimal("15.00"), response.balances().get(0).balance());
        assertEquals(new BigDecimal("-15.00"), response.balances().get(1).balance());
    }

    @Test
    void calculateBalances_ShouldThrowException_WhenUserIsNotMember() {
        UUID partyId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());

        when(membershipRepository.existsByPartyIdAndUserId(partyId, user.getId())).thenReturn(false);

        assertThrows(BusinessException.class, () -> balanceService.calculateBalances(partyId, user));
    }
}