package com.rhacarys.contaconjunta.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record JoinPartyRequest(
        @NotBlank String code,
        @NotBlank @Size(min = 2, max = 50) String alias) {
}