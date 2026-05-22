package com.rhacarys.racharcontas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Usuário e Token JWT de autenticação")
public record LoginResponse(
                @Schema(description = "Token JWT para autenticação nas requisições", example = "eyJhbGciOiJIUzI1NiJ9...") String token,
                @Schema(description = "Perfil do usuário autenticado") UserResponse user) {
}
