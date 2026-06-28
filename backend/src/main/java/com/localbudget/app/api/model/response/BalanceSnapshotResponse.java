package com.localbudget.app.api.model.response;

import java.math.BigDecimal;
import java.time.Instant;

public record BalanceSnapshotResponse(
        String snapshotId,
        Instant syncedAt,
        String accountId,
        String accountName,
        String accountMask,
        BigDecimal currentBalance,
        BigDecimal availableBalance,
        String isoCurrencyCode
) {
}
