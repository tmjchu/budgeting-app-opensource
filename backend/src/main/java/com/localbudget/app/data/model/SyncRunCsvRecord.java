package com.localbudget.app.data.model;

public record SyncRunCsvRecord(
        String syncId,
        String startedAt,
        String finishedAt,
        String status,
        String transactionsAdded,
        String transactionsUpdated,
        String balanceSnapshotsAdded,
        String errorMessage
) {
}
