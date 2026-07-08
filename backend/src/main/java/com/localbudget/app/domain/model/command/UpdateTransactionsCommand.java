package com.localbudget.app.domain.model.command;

import java.time.LocalDate;
import java.util.Set;

public record UpdateTransactionsCommand(
        Set<String> transactionIds, String customName, String categoryId, LocalDate date) {}
