package com.rhacarys.racharcontas.api.dto;

import java.time.Instant;
import java.util.UUID;

import com.rhacarys.racharcontas.domain.model.Membership;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Informações de um participante do grupo")
public record MembershipResponse(
        @Schema(description = "ID único do vínculo (membership)") UUID id,
        @Schema(description = "ID do usuário associado") UUID userId,
        @Schema(description = "Apelido do usuário no grupo") String alias,
        @Schema(description = "Papel do usuário no grupo (ex: ADMIN, MEMBER)") String role,
        @Schema(description = "Data de exclusão, presente apenas se o membro foi removido (soft-delete)") Instant deletedAt) {

    public static MembershipResponse fromEntity(Membership membership) {
        return new MembershipResponse(
                membership.getId(),
                membership.getUser().getId(),
                membership.getAlias(),
                membership.getRole(),
                membership.getDeletedAt());
    }
}