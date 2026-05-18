package com.rhacarys.contaconjunta.domain.event;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Event published immediately after an expense is successfully registered.
 */
public record ExpenseCreatedEvent(
        UUID expenseId,
        UUID partyId,
        String description,
        BigDecimal totalAmount,
        String payerName,
        List<UUID> debtorIds) {
}