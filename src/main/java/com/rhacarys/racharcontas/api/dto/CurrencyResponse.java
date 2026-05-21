package com.rhacarys.racharcontas.api.dto;

import java.util.UUID;

import com.rhacarys.racharcontas.domain.model.Currency;

public record CurrencyResponse(
		UUID id,
		String code,
		String name) {

	public static CurrencyResponse fromEntity(Currency currency) {
		return new CurrencyResponse(
				currency.getId(),
				currency.getCode(),
				currency.getName());
	}
}
