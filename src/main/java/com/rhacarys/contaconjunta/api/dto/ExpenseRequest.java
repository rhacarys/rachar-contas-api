package com.rhacarys.contaconjunta.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.rhacarys.contaconjunta.domain.model.ExpenseType;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para registrar uma despesa")
public record ExpenseRequest(
        @NotBlank @Schema(description = "Descrição da despesa", example = "Almoço") String description,
        @NotNull @Positive @Schema(description = "Valor total da despesa", example = "120.50") BigDecimal amount,
        @NotNull @Schema(description = "Data da despesa") Instant date,
        @NotNull @Schema(description = "ID do usuário que pagou a despesa") UUID payerId,
        @Schema(description = "Tipo de despesa") ExpenseType type,
        @NotEmpty @Schema(description = "Lista de como a despesa foi dividida") List<SplitRequest> splits) {

    @Schema(description = "Divisão da despesa para um membro")
    public record SplitRequest(
            @NotNull @Schema(description = "ID do usuário devedor") UUID debtorId,
            @NotNull @Positive @Schema(description = "Valor devido") BigDecimal amount) {
    }
}
