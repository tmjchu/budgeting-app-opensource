package com.localbudget.app.api.model.response;

import java.math.BigDecimal;

public record CategoryStatsResponse(String category, BigDecimal amount, long transactionCount) {}
