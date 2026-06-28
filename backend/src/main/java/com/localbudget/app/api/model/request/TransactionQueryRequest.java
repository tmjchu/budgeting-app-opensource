package com.localbudget.app.api.model.request;

import java.time.LocalDate;
import java.time.YearMonth;

public record TransactionQueryRequest(
        YearMonth month,
        LocalDate startDate,
        LocalDate endDate,
        String accountId,
        String category
) {
}
