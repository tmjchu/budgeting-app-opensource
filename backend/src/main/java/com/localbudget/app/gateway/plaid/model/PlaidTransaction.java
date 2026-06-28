package com.localbudget.app.gateway.plaid.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PlaidTransaction(
        String plaidItemId,
        String transactionId,
        String accountId,
        String accountName,
        LocalDate date,
        String name,
        String merchantName,
        BigDecimal amount,
        String primaryCategory,
        String detailedCategory,
        boolean pending,
        String paymentChannel) {}
