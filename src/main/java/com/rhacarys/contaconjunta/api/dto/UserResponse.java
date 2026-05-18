package com.rhacarys.contaconjunta.api.dto;

import com.rhacarys.contaconjunta.domain.model.User;
import java.util.UUID;

public record UserResponse(UUID id, String name, String email) {
    public static UserResponse fromEntity(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getLogin());
    }
}