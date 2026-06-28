package com.localbudget.app.domain.model;

import java.math.BigDecimal;
import java.time.Instant;

public record BalanceSnapshot(
        String snapshotId,
        Instant syncedAt,
        String plaidItemId,
        String accountId,
        String accountName,
        String accountMask,
        String accountType,
        String accountSubtype,
        BigDecimal currentBalance,
        BigDecimal availableBalance,
        String isoCurrencyCode,
        String unofficialCurrencyCode) {}
