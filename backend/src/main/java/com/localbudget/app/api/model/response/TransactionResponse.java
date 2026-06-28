package com.localbudget.app.api.model.response;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionResponse(
        String transactionId,
        String accountId,
        String accountName,
        LocalDate date,
        String name,
        String merchantName,
        BigDecimal amount,
        String category,
        boolean pending,
        boolean excluded,
        String paymentChannel) {}
