package com.localbudget.app.data.model;

public record BalanceSnapshotCsvRecord(
        String snapshotId,
        String syncedAt,
        String plaidItemId,
        String accountId,
        String accountName,
        String accountMask,
        String accountType,
        String accountSubtype,
        String currentBalance,
        String availableBalance,
        String isoCurrencyCode,
        String unofficialCurrencyCode
) {
}
