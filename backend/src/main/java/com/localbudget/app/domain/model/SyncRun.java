package com.localbudget.app.domain.model;

import java.time.Instant;

public record SyncRun(
        String syncId,
        Instant startedAt,
        Instant finishedAt,
        SyncStatus status,
        int transactionsAdded,
        int transactionsUpdated,
        int balanceSnapshotsAdded,
        String errorMessage) {}
