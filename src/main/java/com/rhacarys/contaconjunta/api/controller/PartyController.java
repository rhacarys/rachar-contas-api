package com.rhacarys.contaconjunta.api.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.rhacarys.contaconjunta.api.dto.JoinPartyRequest;
import com.rhacarys.contaconjunta.api.dto.PartyRequest;
import com.rhacarys.contaconjunta.api.dto.PartyResponse;
import com.rhacarys.contaconjunta.domain.model.User;
import com.rhacarys.contaconjunta.domain.service.PartyService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/parties")
@RequiredArgsConstructor
public class PartyController {

    private final PartyService partyService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PartyResponse createParty(
            @RequestBody @Valid PartyRequest request,
            @AuthenticationPrincipal User loggedUser) {
        return partyService.createParty(request, loggedUser);
    }

    @GetMapping
    public List<PartyResponse> getUserParties(@AuthenticationPrincipal User loggedUser) {
        return partyService.getUserParties(loggedUser);
    }

    @PostMapping("/join")
    public PartyResponse joinParty(
            @RequestBody @Valid JoinPartyRequest request,
            @AuthenticationPrincipal User loggedUser) {
        return partyService.joinParty(request, loggedUser);
    }
}