package com.localbudget.app;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.domain.model.Account;
import com.localbudget.app.domain.model.PlaidItem;
import com.localbudget.app.domain.model.Transaction;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class TestFixtures {

    private TestFixtures() {
    }

    public static BudgetAppProperties properties(Path dataDirectory) {
        return new BudgetAppProperties(
                dataDirectory,
                new BudgetAppProperties.Plaid(
                        "https://sandbox.plaid.com",
                        "client-id",
                        "secret",
                        "Local Budget",
                        List.of("transactions"),
                        List.of("US")));
    }

    public static Account checkingAccount() {
        return new Account("acc-checking", "item-1", "Main Checking", "1234", "depository", "checking", true);
    }

    public static PlaidItem plaidItem() {
        return new PlaidItem("item-1", "access-token", "Test Bank", Instant.parse("2026-01-01T00:00:00Z"));
    }

    public static Transaction transaction(String id, LocalDate date, BigDecimal amount, String category) {
        return new Transaction(
                id,
                "item-1",
                "acc-checking",
                "Main Checking",
                date,
                "Transaction " + id,
                "Merchant " + id,
                amount,
                category,
                category == null ? null : category + "_DETAIL",
                null,
                false,
                false,
                "in store");
    }
}
