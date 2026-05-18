package com.rhacarys.contaconjunta.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.rhacarys.contaconjunta.domain.model.Expense;

public record ExpenseResponse(
        UUID id,
        String description,
        BigDecimal amount,
        Instant date,
        UUID payerId,
        List<SplitResponse> splits) {
    public record SplitResponse(
            UUID debtorId,
            BigDecimal amount,
            boolean isSettled) {
    }

    public static ExpenseResponse fromEntity(Expense expense) {
        List<SplitResponse> splitResponses = expense.getSplits().stream()
                .map(split -> new SplitResponse(
                        split.getDebtor().getId(),
                        split.getAmount(),
                        split.isSettled()))
                .toList();

        return new ExpenseResponse(
                expense.getId(),
                expense.getDescription(),
                expense.getAmount(),
                expense.getDate(),
                expense.getPayer().getId(),
                splitResponses);
    }
}