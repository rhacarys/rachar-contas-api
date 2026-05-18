package com.rhacarys.contaconjunta.api.dto;

import java.util.UUID;

import com.rhacarys.contaconjunta.domain.model.Party;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informações de um grupo")
public record PartyResponse(
        @Schema(description = "ID único do grupo") UUID id,
        @Schema(description = "Código único do grupo para convites", example = "ABC123") String code,
        @Schema(description = "Nome do grupo") String name,
        @Schema(description = "Descrição do grupo") String description,
        @Schema(description = "Moeda utilizada no grupo") String currencyCode) {

public static PartyResponse fromEntity(Party party) {
        return new PartyResponse(
                party.getId(),
                party.getCode(),
                party.getName(),
                party.getDescription(),
                party.getCurrency().getCode());
        }
}