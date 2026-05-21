package com.rhacarys.racharcontas.api.dto;

import java.math.BigDecimal;
import java.util.UUID;

import com.rhacarys.racharcontas.domain.model.Party;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informações de um grupo")
public record PartyResponse(
                @Schema(description = "ID único do grupo") UUID id,
                @Schema(description = "Código único do grupo para convites", example = "ABC123") String code,
                @Schema(description = "Nome do grupo") String name,
                @Schema(description = "Descrição do grupo") String description,
                @Schema(description = "Moeda utilizada no grupo") String currencyCode,
                @Schema(description = "Saldo atual do usuário no grupo. Positivo = é devido, Negativo = deve") BigDecimal userBalance) {

        public static PartyResponse fromEntity(Party party, BigDecimal userBalance) {
                return new PartyResponse(
                                party.getId(),
                                party.getCode(),
                                party.getName(),
                                party.getDescription(),
                                party.getCurrency().getCode(),
                                userBalance);
        }
}