package com.rhacarys.contaconjunta.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ExpenseRequest(
        @NotBlank String description,
        @NotNull @Positive BigDecimal amount,
        @NotNull Instant date,
        @NotNull UUID payerId,
        @NotEmpty List<SplitRequest> splits) {

    public record SplitRequest(
            @NotNull UUID debtorId,
            @NotNull @Positive BigDecimal amount) {
    }
}
