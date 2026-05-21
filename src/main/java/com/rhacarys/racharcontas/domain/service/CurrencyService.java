package com.rhacarys.racharcontas.domain.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.rhacarys.racharcontas.api.dto.CurrencyResponse;
import com.rhacarys.racharcontas.domain.repository.CurrencyRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class CurrencyService {

	private final CurrencyRepository currencyRepository;

	@Transactional(readOnly = true)
	public List<CurrencyResponse> getAllCurrencies() {
		log.debug("Fetching all currencies");

		List<CurrencyResponse> currencies = currencyRepository.findAll().stream()
				.map(CurrencyResponse::fromEntity)
				.toList();

		log.debug("Found {} currencies", currencies.size());
		return currencies;
	}
}
