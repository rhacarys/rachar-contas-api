package com.rhacarys.racharcontas.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Dados para criar ou atualizar um grupo")
public record PartyRequest(
        @NotBlank @Size(min = 3, max = 100) @Schema(description = "Nome do grupo (3-100 caracteres)", example = "Viagem para Praia") String name,
        @Schema(description = "Descrição opcional do grupo", example = "Grupo para dividir custos da viagem") String description,
        @NotBlank @Schema(description = "Código ISO da moeda", example = "BRL") String currencyCode
) {
}