package com.rhacarys.racharcontas.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rhacarys.racharcontas.api.dto.LoginRequest;
import com.rhacarys.racharcontas.api.dto.LoginResponse;
import com.rhacarys.racharcontas.api.dto.RegisterRequest;
import com.rhacarys.racharcontas.domain.service.AuthService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Autenticação e registro de usuários")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar novo usuário")
    public void register(@RequestBody @Valid RegisterRequest data) {
        authService.register(data);
    }

    @PostMapping("/login")
    @Operation(summary = "Fazer login e obter token JWT")
    public LoginResponse login(@RequestBody @Valid LoginRequest data) {
        var token = authService.login(data);
        return new LoginResponse(token);
    }
}