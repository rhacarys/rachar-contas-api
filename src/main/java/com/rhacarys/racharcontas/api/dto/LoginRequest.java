package com.rhacarys.racharcontas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Credenciais para fazer login")
public record LoginRequest(
                @NotBlank @Schema(description = "Usuário ou email", example = "joao@email.com") String login,
                @NotBlank @Size(min = 6, max = 16, message = "A senha deve ter entre 6 e 16 caracteres") @Schema(description = "Senha", example = "senha123") String password) {
}
