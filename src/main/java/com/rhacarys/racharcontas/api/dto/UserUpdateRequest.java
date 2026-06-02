package com.rhacarys.racharcontas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para atualizar o perfil do usuário")
public record UserUpdateRequest(
                @NotBlank @Size(min = 3, max = 50) @Schema(description = "Novo nome do usuário", example = "João Silva") String name,
                @Size(max = 72) @Schema(description = "Senha atual (obrigatória se quiser alterar a senha)") String currentPassword,
                @Size(min = 6, max = 16, message = "A nova senha deve ter entre 6 e 16 caracteres") @Schema(description = "Nova senha (entre 6 e 16 caracteres)") String newPassword) {
}
