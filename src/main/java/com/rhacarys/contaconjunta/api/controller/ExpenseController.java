package com.rhacarys.contaconjunta.api.controller;

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

import com.rhacarys.contaconjunta.api.dto.ExpenseRequest;
import com.rhacarys.contaconjunta.api.dto.ExpenseResponse;
import com.rhacarys.contaconjunta.domain.model.User;
import com.rhacarys.contaconjunta.domain.service.ExpenseService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/parties/{partyId}/expenses")
@RequiredArgsConstructor
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ExpenseResponse createExpense(
            @PathVariable UUID partyId,
            @RequestBody @Valid ExpenseRequest request,
            @AuthenticationPrincipal User loggedUser) {
        return expenseService.createExpense(partyId, request, loggedUser);
    }

    @GetMapping
    public List<ExpenseResponse> getPartyExpenses(
            @PathVariable UUID partyId,
            @AuthenticationPrincipal User loggedUser) {
        return expenseService.getPartyExpenses(partyId, loggedUser);
    }

    @DeleteMapping("/{expenseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteExpense(
            @PathVariable UUID partyId,
            @PathVariable UUID expenseId,
            @AuthenticationPrincipal User loggedUser) {
        expenseService.deleteExpense(partyId, expenseId, loggedUser);
    }
}