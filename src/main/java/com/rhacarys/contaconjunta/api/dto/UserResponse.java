package com.rhacarys.contaconjunta.api.dto;

import com.rhacarys.contaconjunta.domain.model.User;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informações do usuário")
public record UserResponse(
        @Schema(description = "ID único do usuário") UUID id,
        @Schema(description = "Nome do usuário") String name,
        @Schema(description = "Login do usuário") String login) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getLogin());
    }
}