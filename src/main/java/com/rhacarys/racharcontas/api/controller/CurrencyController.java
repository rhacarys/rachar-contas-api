package com.rhacarys.racharcontas.api.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.rhacarys.racharcontas.api.dto.CurrencyResponse;
import com.rhacarys.racharcontas.domain.service.CurrencyService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor
@Tag(name = "Currencies", description = "Gerenciamento de moedas")
public class CurrencyController {

	private final CurrencyService currencyService;

	@GetMapping
	@Operation(summary = "Listar todas as moedas")
	public ResponseEntity<List<CurrencyResponse>> getAllCurrencies() {
		List<CurrencyResponse> currencies = currencyService.getAllCurrencies();
		return ResponseEntity.ok(currencies);
	}
}
