package com.rhacarys.contaconjunta.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para registrar um novo usuário")
public record RegisterRequest(
        @NotBlank @Schema(description = "Nome do usuário", example = "João Silva") String name,
        @NotBlank @Size(min = 3, max = 50) @Schema(description = "Usuário ou email (3-50 caracteres)", example = "joao@email.com") String login,
        @NotBlank @Size(min = 6) @Schema(description = "Senha (mínimo 6 caracteres)", example = "senha123") String password) {
}