package com.localbudget.app.data.model;

public record TransactionCsvRecord(
        String transactionId,
        String plaidItemId,
        String accountId,
        String accountName,
        String date,
        String name,
        String merchantName,
        String amount,
        String primaryCategory,
        String detailedCategory,
        String localCategory,
        String pending,
        String excluded,
        String paymentChannel) {}
