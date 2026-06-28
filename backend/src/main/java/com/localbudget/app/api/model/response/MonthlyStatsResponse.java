package com.localbudget.app.api.model.response;

import java.math.BigDecimal;

public record MonthlyStatsResponse(
        String month,
        BigDecimal income,
        BigDecimal spending,
        BigDecimal netCashFlow,
        long transactionCount
) {
}
