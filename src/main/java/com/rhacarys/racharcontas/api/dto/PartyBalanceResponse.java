package com.rhacarys.racharcontas.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Saldo e divisão de despesas do grupo")
public record PartyBalanceResponse(
        @Schema(description = "ID do grupo") UUID partyId,
        @Schema(description = "Saldo de cada membro do grupo") List<MemberBalance> balances) {
    
    @Schema(description = "Saldo de um membro no grupo")
    public record MemberBalance(
            @Schema(description = "ID da participação do membro no grupo") UUID membershipId,
            @Schema(description = "ID do usuário") UUID userId,
            @Schema(description = "Apelido do membro no grupo") String alias,
            @Schema(description = "Cargo do membro no grupo") String role,
            @Schema(description = "Saldo positivo = deve receber, negativo = deve pagar") BigDecimal balance) {
    }
}