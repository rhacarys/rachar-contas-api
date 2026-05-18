package com.rhacarys.contaconjunta.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Token JWT de autenticação")
public record LoginResponse(
        @Schema(description = "Token JWT para autenticação nas requisições", example = "eyJhbGciOiJIUzI1NiJ9...") String token) {
}
