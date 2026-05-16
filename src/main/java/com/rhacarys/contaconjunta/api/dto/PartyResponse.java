package com.rhacarys.contaconjunta.api.dto;

import java.util.UUID;

public record PartyResponse(
        UUID id,
        String code,
        String name,
        String description,
        String currencyCode) {
}