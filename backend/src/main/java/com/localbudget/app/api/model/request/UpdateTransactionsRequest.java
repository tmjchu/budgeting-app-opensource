package com.localbudget.app.api.model.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import java.time.LocalDate;
import java.util.Set;

public record UpdateTransactionsRequest(
        @NotEmpty Set<@NotBlank String> transactionIds,
        String customName,
        String categoryId,
        LocalDate date) {}
