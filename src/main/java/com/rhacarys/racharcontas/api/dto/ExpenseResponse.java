package com.rhacarys.racharcontas.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.rhacarys.racharcontas.domain.model.Expense;
import com.rhacarys.racharcontas.domain.model.ExpenseType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informações de uma despesa registrada")
public record ExpenseResponse(
                @Schema(description = "ID único da despesa") UUID id,
                @Schema(description = "Descrição da despesa") String description,
                @Schema(description = "Valor total da despesa") BigDecimal amount,
                @Schema(description = "Data da despesa") Instant date,
                @Schema(description = "ID do usuário que pagou") UUID payerId,
                @Schema(description = "Tipo de despesa") ExpenseType type,
                @Schema(description = "Como a despesa foi dividida") List<SplitResponse> splits) {
        
        @Schema(description = "Divisão da despesa para um membro")
        public record SplitResponse(
                        @Schema(description = "ID do usuário devedor") UUID debtorId,
                        @Schema(description = "Valor devido") BigDecimal amount) {
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