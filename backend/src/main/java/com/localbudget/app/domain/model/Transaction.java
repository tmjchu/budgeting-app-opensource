package com.localbudget.app.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record Transaction(
        String transactionId,
        String plaidItemId,
        String accountId,
        String accountName,
        LocalDate date,
        String name,
        String merchantName,
        BigDecimal amount,
        String primaryCategory,
        String detailedCategory,
        String localCategory,
        boolean pending,
        boolean excluded,
        String paymentChannel
) {
    public String effectiveCategory() {
        if (localCategory != null && !localCategory.isBlank()) {
            return localCategory;
        }
        if (primaryCategory != null && !primaryCategory.isBlank()) {
            return primaryCategory;
        }
        return "Uncategorized";
    }
}
