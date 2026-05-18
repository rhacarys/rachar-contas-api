package com.rhacarys.contaconjunta.api.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record PartyBalanceResponse(
        UUID partyId,
        List<MemberBalance> balances) {
    public record MemberBalance(
            UUID membershipId,
            String alias,
            BigDecimal balance) {
    }
}