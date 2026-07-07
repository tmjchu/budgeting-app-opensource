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
}
