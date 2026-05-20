package com.rhacarys.racharcontas.domain.service;

import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rhacarys.racharcontas.api.dto.ExpenseRequest;
import com.rhacarys.racharcontas.domain.exception.BusinessException;
import com.rhacarys.racharcontas.domain.model.User;
import com.rhacarys.racharcontas.domain.repository.ExpenseRepository;
import com.rhacarys.racharcontas.domain.repository.MembershipRepository;
import com.rhacarys.racharcontas.domain.repository.PartyRepository;

@ExtendWith(MockitoExtension.class)
class ExpenseServiceTest {

    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private PartyRepository partyRepository;
    @Mock
    private MembershipRepository membershipRepository;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void createExpense_ShouldThrowException_WhenSumOfSplitsMismatchesTotalAmount() {
        UUID partyId = UUID.randomUUID();
        User user = new User();
        user.setId(UUID.randomUUID());

        ExpenseRequest request = new ExpenseRequest(
                "Pizza",
                new BigDecimal("100.00"),
                Instant.now(),
                UUID.randomUUID(),
                null,
                List.of(new ExpenseRequest.SplitRequest(UUID.randomUUID(), new BigDecimal("45.00"))));

        assertThrows(BusinessException.class, () -> expenseService.createExpense(partyId, request, user));
    }
}