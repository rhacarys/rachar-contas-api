package com.rhacarys.racharcontas.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Dados para entrar em um grupo existente")
public record UpdateAliasRequest(
        @NotBlank @Size(min = 2, max = 50) @Schema(description = "Apelido do membro no grupo", example = "João") String alias) {
}