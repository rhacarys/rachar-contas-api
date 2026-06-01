package com.rhacarys.racharcontas.api.dto;

import java.time.Instant;
import java.util.List;

public record SyncResponse(
        PartyResponse party,
        List<MembershipResponse> memberships,
        List<ExpenseResponse> expenses,
        Instant serverTimestamp) {
}