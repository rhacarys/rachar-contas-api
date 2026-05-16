package com.rhacarys.contaconjunta.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PartyRequest(
        @NotBlank @Size(min = 3, max = 100) String name,
        String description,
        @NotBlank String currencyCode
) {
}