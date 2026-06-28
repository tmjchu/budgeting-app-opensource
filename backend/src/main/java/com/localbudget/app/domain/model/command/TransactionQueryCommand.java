package com.localbudget.app.domain.model.command;

import java.time.LocalDate;
import java.time.YearMonth;

public record TransactionQueryCommand(
        YearMonth month,
        LocalDate startDate,
        LocalDate endDate,
        String accountId,
        String category
) {
}
