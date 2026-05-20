package com.rhacarys.racharcontas.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rhacarys.racharcontas.api.dto.UserResponse;
import com.rhacarys.racharcontas.api.dto.UserUpdateRequest;
import com.rhacarys.racharcontas.domain.model.User;
import com.rhacarys.racharcontas.domain.service.UserService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/users/me")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Gerenciamento do perfil do usuário autenticado")
public class UserController {

    private final UserService userService;

    @PutMapping
    @Operation(summary = "Atualizar perfil do usuário")
    public UserResponse updateMe(
            @RequestBody @Valid UserUpdateRequest request,
            @AuthenticationPrincipal User loggedUser) {
        return userService.updateMe(request, loggedUser);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar conta do usuário")
    public void deleteMe(@AuthenticationPrincipal User loggedUser) {
        userService.deleteMe(loggedUser);
    }
}