package com.rhacarys.racharcontas.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/v1")
@Tag(name = "Health", description = "Verificação de saúde da API")
public class HealthController {

    @GetMapping("/ping")
    @SecurityRequirements()
    @Operation(summary = "Verificar se a API está disponível")
    public ResponseEntity<String> keepAlive() {
        return ResponseEntity.ok("pong");
    }
}