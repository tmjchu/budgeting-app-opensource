package com.localbudget.app.domain.model;

import java.math.BigDecimal;

public record CategoryStats(
        String category,
        BigDecimal amount,
        long transactionCount
) {
}
