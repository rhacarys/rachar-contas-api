package com.rhacarys.contaconjunta.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para entrar em um grupo existente")
public record JoinPartyRequest(
        @NotBlank @Schema(description = "Código do grupo para convite", example = "ABC123") String code,
        @NotBlank @Size(min = 2, max = 50) @Schema(description = "Apelido do usuário no grupo (2-50 caracteres)", example = "João") String alias) {
}