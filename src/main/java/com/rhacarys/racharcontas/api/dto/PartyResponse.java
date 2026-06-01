package com.rhacarys.racharcontas.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.rhacarys.racharcontas.domain.model.Party;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informações de um grupo de despesas")
public record PartyResponse(
                @Schema(description = "ID único do grupo") UUID id,
                @Schema(description = "Código de convite único de 8 caracteres") String code,
                @Schema(description = "Nome do grupo") String name,
                @Schema(description = "Descrição opcional") String description,
                @Schema(description = "Código da moeda (ex: BRL, USD)") String currencyCode,
                @Schema(description = "Saldo do usuário neste grupo (positivo para a receber, negativo para a pagar)") BigDecimal myBalance,
                @Schema(description = "Data de exclusão, presente apenas se o grupo foi removido (soft-delete)") Instant deletedAt) {

        public static PartyResponse fromEntity(Party party, BigDecimal balance) {
                return new PartyResponse(
                                party.getId(),
                                party.getCode(),
                                party.getName(),
                                party.getDescription(),
                                party.getCurrency().getCode(),
                                balance,
                                party.getDeletedAt());
        }
}