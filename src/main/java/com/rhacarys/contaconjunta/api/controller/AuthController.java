package com.rhacarys.contaconjunta.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rhacarys.contaconjunta.api.dto.LoginRequest;
import com.rhacarys.contaconjunta.api.dto.LoginResponse;
import com.rhacarys.contaconjunta.api.dto.RegisterRequest;
import com.rhacarys.contaconjunta.domain.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody @Valid RegisterRequest data) {
        authService.register(data);
    }

    @PostMapping("/login")
    public LoginResponse login(@RequestBody @Valid LoginRequest data) {
        var token = authService.login(data);
        return new LoginResponse(token);
    }
}