package com.rhacarys.racharcontas.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rhacarys.racharcontas.api.dto.ExpenseRequest;
import com.rhacarys.racharcontas.api.dto.ExpenseResponse;
import com.rhacarys.racharcontas.domain.model.User;
import com.rhacarys.racharcontas.domain.service.ExpenseService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/parties/{partyId}/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Gerenciamento de despesas dentro de grupos")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar nova despesa no grupo")
    public ExpenseResponse createExpense(
            @PathVariable UUID partyId,
            @RequestBody @Valid ExpenseRequest request,
            @AuthenticationPrincipal User loggedUser) {
        return expenseService.createExpense(partyId, request, loggedUser);
    }

    @GetMapping
    @Operation(summary = "Listar todas as despesas do grupo")
    public List<ExpenseResponse> getPartyExpenses(
            @PathVariable UUID partyId,
            @AuthenticationPrincipal User loggedUser) {
        return expenseService.getPartyExpenses(partyId, loggedUser);
    }

    @DeleteMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Deletar uma despesa")
    public void deleteExpense(
            @PathVariable UUID partyId,
            @PathVariable UUID expenseId,
            @AuthenticationPrincipal User loggedUser) {
        expenseService.deleteExpense(partyId, expenseId, loggedUser);
    }
}