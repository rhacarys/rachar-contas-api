package com.rhacarys.racharcontas.api.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import com.rhacarys.racharcontas.domain.model.ExpenseType;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Payload contendo todas as alterações feitas offline para sincronização em lote")
public record SyncPushRequest(
        @Schema(description = "Despesas criadas offline") List<ExpenseSyncPayload> expensesToCreate,
        @Schema(description = "IDs de despesas deletadas offline") List<UUID> expensesToDelete) {
    public record ExpenseSyncPayload(
            UUID id,
            UUID payerId,
            String description,
            BigDecimal amount,
            Instant date,
            ExpenseType type,
            List<ExpenseRequest.SplitRequest> splits) {
    }
}