package com.rhacarys.contaconjunta.api.dto;

import java.util.UUID;

import com.rhacarys.contaconjunta.domain.model.Party;

public record PartyResponse(
        UUID id,
        String code,
        String name,
        String description,
        String currencyCode) {

public static PartyResponse fromEntity(Party party) {
        return new PartyResponse(
                party.getId(),
                party.getCode(),
                party.getName(),
                party.getDescription(),
                party.getCurrency().getCode());
        }
}