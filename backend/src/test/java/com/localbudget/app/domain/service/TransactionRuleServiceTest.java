package com.localbudget.app.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.domain.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class TransactionRuleServiceTest {

    private final TransactionRuleService service = new TransactionRuleService();

    @Test
    void applyRulesMarksPlaidTransfersAsExcluded() {
        Transaction transfer =
                TestFixtures.transaction(
                        "transfer",
                        LocalDate.parse("2026-06-01"),
                        new BigDecimal("100.00"),
                        "TRANSFER");
        Transaction food =
                TestFixtures.transaction(
                        "food",
                        LocalDate.parse("2026-06-02"),
                        new BigDecimal("25.00"),
                        "FOOD_AND_DRINK");

        List<Transaction> normalized = service.applyRules(List.of(transfer, food));

        assertThat(normalized.get(0).excluded()).isTrue();
        assertThat(normalized.get(1).excluded()).isFalse();
    }
}
