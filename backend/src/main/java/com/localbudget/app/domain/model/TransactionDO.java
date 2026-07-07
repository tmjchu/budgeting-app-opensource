package com.localbudget.app.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;

public record TransactionDO(
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
        String localCategoryId,
        boolean pending,
        boolean excluded,
        String paymentChannel) {
    public String effectiveCategory() {
        if (localCategory != null && !localCategory.isBlank()) {
            return localCategory;
        }
        if (primaryCategory != null && !primaryCategory.isBlank()) {
            return primaryCategory;
        }
        return "Uncategorized";
    }

    public TransactionDO withLocalCategoryId(String nextLocalCategoryId) {
        return new TransactionDO(
                transactionId,
                plaidItemId,
                accountId,
                accountName,
                date,
                name,
                merchantName,
                amount,
                primaryCategory,
                detailedCategory,
                null,
                nextLocalCategoryId,
                pending,
                excluded,
                paymentChannel);
    }

    public TransactionDO withLocalCategoryIdIfUnassigned(String nextLocalCategoryId) {
        if (localCategoryId != null && !localCategoryId.isBlank()) {
            return this;
        }
        if (localCategory != null && !localCategory.isBlank()) {
            return this;
        }
        return withLocalCategoryId(nextLocalCategoryId);
    }
}
