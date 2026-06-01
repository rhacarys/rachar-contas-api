package com.rhacarys.racharcontas.api.controller;

import java.time.Instant;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.rhacarys.racharcontas.api.dto.SyncPushRequest;
import com.rhacarys.racharcontas.api.dto.SyncResponse;
import com.rhacarys.racharcontas.domain.model.User;
import com.rhacarys.racharcontas.domain.service.SyncService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/parties/{partyId}/sync")
@RequiredArgsConstructor
@Tag(name = "Sync", description = "Endpoints para sincronização offline-first")
public class SyncController {

    private final SyncService syncService;

    @GetMapping
    @Operation(summary = "Busca alterações ocorridas no grupo desde a última sincronização")
    public ResponseEntity<SyncResponse> getUpdatesSince(
            @PathVariable UUID partyId,
            @RequestParam Instant lastSync,
            @AuthenticationPrincipal User user) {

        SyncResponse response = syncService.getUpdatesSince(partyId, lastSync, user);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @Operation(summary = "Envia alterações feitas em modo offline para o servidor em lote (Upsert e Delete)")
    public ResponseEntity<Void> pushOfflineChanges(
            @PathVariable UUID partyId,
            @RequestBody SyncPushRequest request,
            @AuthenticationPrincipal User user) {

        syncService.processBatchSync(partyId, request, user);
        return ResponseEntity.ok().build();
    }
}