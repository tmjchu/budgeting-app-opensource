package com.localbudget.app.gateway.plaid.model;

import java.math.BigDecimal;

public record PlaidBalance(
        String plaidItemId,
        String accountId,
        String accountName,
        String accountMask,
        String accountType,
        String accountSubtype,
        BigDecimal currentBalance,
        BigDecimal availableBalance,
        String isoCurrencyCode,
        String unofficialCurrencyCode
) {
}
