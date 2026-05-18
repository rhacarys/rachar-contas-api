package com.rhacarys.contaconjunta.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.rhacarys.contaconjunta.domain.model.Expense;
import com.rhacarys.contaconjunta.domain.model.ExpenseType;

public record ExpenseResponse(
                UUID id,
                String description,
                BigDecimal amount,
                Instant date,
                UUID payerId,
                ExpenseType type,
                List<SplitResponse> splits) {
        public record SplitResponse(
                        UUID debtorId,
                        BigDecimal amount) {
        }

        public static ExpenseResponse fromEntity(Expense expense) {
                List<SplitResponse> splitResponses = expense.getSplits().stream()
                                .map(split -> new SplitResponse(split.getDebtor().getId(), split.getAmount()))
                                .toList();

                return new ExpenseResponse(
                                expense.getId(),
                                expense.getDescription(),
                                expense.getAmount(),
                                expense.getDate(),
                                expense.getPayer().getId(),
                                expense.getType(),
                                splitResponses);
        }
}