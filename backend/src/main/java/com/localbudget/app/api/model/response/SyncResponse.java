package com.localbudget.app.api.model.response;

import java.time.Instant;

public record SyncResponse(
        String syncId,
        String status,
        Instant startedAt,
        Instant finishedAt,
        int transactionsAdded,
        int transactionsUpdated,
        int balanceSnapshotsAdded,
        String errorMessage) {}
