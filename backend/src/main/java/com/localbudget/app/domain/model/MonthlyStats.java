package com.localbudget.app.domain.model;

import java.math.BigDecimal;
import java.time.YearMonth;

public record MonthlyStats(
        YearMonth month,
        BigDecimal income,
        BigDecimal spending,
        BigDecimal netCashFlow,
        long transactionCount) {}
