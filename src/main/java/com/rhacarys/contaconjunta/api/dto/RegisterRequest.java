package com.rhacarys.contaconjunta.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank String name,
        @NotBlank @Size(min = 3, max = 50) String login,
        @NotBlank @Size(min = 6) String password) {
}