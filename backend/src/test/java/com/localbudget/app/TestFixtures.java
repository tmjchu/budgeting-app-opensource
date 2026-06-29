package com.localbudget.app;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.config.BudgetAppProperties.Environment;
import com.localbudget.app.domain.model.AccountDO;
import com.localbudget.app.domain.model.PlaidItem;
import com.localbudget.app.domain.model.TransactionDO;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;

public final class TestFixtures {

    private TestFixtures() {}

    public static BudgetAppProperties properties(Path dataDirectory) {
        return new BudgetAppProperties(
                dataDirectory,
                new BudgetAppProperties.PlaidConfig(
                        Environment.SANDBOX,
                        "client-id",
                        "secret",
                        "Local Budget",
                        List.of("transactions"),
                        List.of("US")));
    }

    public static AccountDO checkingAccount() {
        return new AccountDO(
                "acc-checking", "item-1", "Main Checking", "1234", "depository", "checking", true);
    }

    public static PlaidItem plaidItem() {
        return new PlaidItem(
                "item-1", "access-token", "Test Bank", Instant.parse("2026-01-01T00:00:00Z"));
    }

    public static TransactionDO transaction(
            String id, LocalDate date, BigDecimal amount, String category) {
        return new TransactionDO(
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
