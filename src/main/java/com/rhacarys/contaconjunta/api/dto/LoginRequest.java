package com.rhacarys.contaconjunta.api.dto;

import jakarta.validation.constraints.NotBlank;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciais para fazer login")
public record LoginRequest(
        @NotBlank @Schema(description = "Usuário ou email", example = "joao@email.com") String login,
        @NotBlank @Schema(description = "Senha", example = "senha123") String password) {
}
