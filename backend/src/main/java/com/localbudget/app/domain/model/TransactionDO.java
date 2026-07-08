package com.localbudget.app.domain.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;

@Getter
@Setter
@ToString
@EqualsAndHashCode
@AllArgsConstructor
@Accessors(fluent = true)
public class TransactionDO {

    private String transactionId;
    private String plaidItemId;
    private String accountId;
    private String accountName;
    private LocalDate date;
    private String name;
    private String merchantName;
    private BigDecimal amount;
    private String primaryCategory;
    private String detailedCategory;
    private String localCategory;
    private String localCategoryId;
    private boolean pending;
    private boolean excluded;
    private String paymentChannel;
    private String customName;
    private LocalDate customDate;

    public TransactionDO(
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
        this(
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
                localCategory,
                localCategoryId,
                pending,
                excluded,
                paymentChannel,
                null,
                null);
    }

    public String effectiveCategory() {
        if (localCategory != null && !localCategory.isBlank()) {
            return localCategory;
        }
        if (primaryCategory != null && !primaryCategory.isBlank()) {
            return primaryCategory;
        }
        return "Uncategorized";
    }

    public String effectiveName() {
        if (customName != null && !customName.isBlank()) {
            return customName;
        }
        return name;
    }

    public LocalDate effectiveDate() {
        return customDate == null ? date : customDate;
    }

    public TransactionDO withLocalCategoryId(String nextLocalCategoryId) {
        localCategory = null;
        localCategoryId = nextLocalCategoryId;
        return this;
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

    public TransactionDO withExcluded(boolean nextExcluded) {
        excluded = nextExcluded;
        return this;
    }

    public TransactionDO withCustomName(String nextCustomName) {
        customName = nextCustomName;
        return this;
    }

    public TransactionDO withCustomDate(LocalDate nextCustomDate) {
        customDate = nextCustomDate;
        return this;
    }
}
