package com.rhacarys.racharcontas.api.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rhacarys.racharcontas.api.dto.JoinPartyRequest;
import com.rhacarys.racharcontas.api.dto.PartyBalanceResponse;
import com.rhacarys.racharcontas.api.dto.PartyRequest;
import com.rhacarys.racharcontas.api.dto.PartyResponse;
import com.rhacarys.racharcontas.api.dto.UpdateAliasRequest;
import com.rhacarys.racharcontas.domain.model.User;
import com.rhacarys.racharcontas.domain.service.BalanceService;
import com.rhacarys.racharcontas.domain.service.PartyService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/parties")
@RequiredArgsConstructor
@Tag(name = "Parties", description = "Gerenciamento de grupos e despesas compartilhadas")
public class PartyController {

    private final PartyService partyService;
    private final BalanceService balanceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Criar novo grupo")
    public PartyResponse createParty(
            @RequestBody @Valid PartyRequest request,
            @AuthenticationPrincipal User loggedUser) {
        return partyService.createParty(request, loggedUser);
    }

    @GetMapping
    @Operation(summary = "Listar todos os grupos do usuário")
    public List<PartyResponse> getUserParties(@AuthenticationPrincipal User loggedUser) {
        return partyService.getUserParties(loggedUser);
    }

    @GetMapping("/{partyId}")
    @Operation(summary = "Obter detalhes de um grupo específico")
    public PartyResponse getParty(
            @PathVariable UUID partyId,
            @AuthenticationPrincipal User loggedUser) {
        return partyService.getParty(partyId, loggedUser);
    }

    @PostMapping("/join")
    @Operation(summary = "Entrar em um grupo existente")
    public PartyResponse joinParty(
            @RequestBody @Valid JoinPartyRequest request,
            @AuthenticationPrincipal User loggedUser) {
        return partyService.joinParty(request, loggedUser);
    }

    @GetMapping("/{partyId}/balances")
    @Operation(summary = "Calcular saldo e divisão de despesas do grupo")
    public PartyBalanceResponse getPartyBalances(
            @PathVariable UUID partyId,
            @AuthenticationPrincipal User loggedUser) {
        return balanceService.calculateBalances(partyId, loggedUser);
    }

    @PutMapping("/{partyId}")
    @Operation(summary = "Atualizar informações do grupo")
    public PartyResponse updateParty(
            @PathVariable UUID partyId,
            @RequestBody @Valid PartyRequest request,
            @AuthenticationPrincipal User loggedUser) {
        return partyService.updateParty(partyId, request, loggedUser);
    }

    @DeleteMapping("/{partyId}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Sair de um grupo")
    public void leaveParty(
            @PathVariable UUID partyId,
            @AuthenticationPrincipal User loggedUser) {
        partyService.leaveParty(partyId, loggedUser);
    }

    @DeleteMapping("/{partyId}/members/{membershipId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover membro do grupo")
    public void kickMember(
            @PathVariable UUID partyId,
            @PathVariable UUID membershipId,
            @AuthenticationPrincipal User loggedUser) {
        partyService.kickMember(partyId, membershipId, loggedUser);
    }

    @PutMapping("/{partyId}/members/me/alias")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Editar meu apelido (alias) no grupo")
    public void updateMyAlias(
            @PathVariable UUID partyId,
            @RequestBody @Valid UpdateAliasRequest request,
            @AuthenticationPrincipal User loggedUser) {
        partyService.updateMyAlias(partyId, loggedUser, request);
    }
}