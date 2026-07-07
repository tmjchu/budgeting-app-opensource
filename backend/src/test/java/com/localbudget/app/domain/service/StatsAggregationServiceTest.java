package com.localbudget.app.domain.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.converter.CategoryConverter;
import com.localbudget.app.data.repository.CategoryCsvRepository;
import com.localbudget.app.domain.model.CategoryStats;
import com.localbudget.app.domain.model.MonthlyStats;
import com.localbudget.app.domain.model.TransactionDO;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class StatsAggregationServiceTest {

    @TempDir java.nio.file.Path dataDirectory;

    @Test
    void buildMonthlyStatsTreatsPositiveAmountsAsSpendingAndNegativeAmountsAsIncome() {
        List<TransactionDO> transactions =
                List.of(
                        TestFixtures.transaction(
                                "coffee",
                                LocalDate.parse("2026-06-01"),
                                new BigDecimal("5.25"),
                                "FOOD"),
                        TestFixtures.transaction(
                                "rent",
                                LocalDate.parse("2026-06-02"),
                                new BigDecimal("1800.00"),
                                "RENT"),
                        TestFixtures.transaction(
                                "pay",
                                LocalDate.parse("2026-06-03"),
                                new BigDecimal("-4000.00"),
                                "PAYROLL"),
                        TestFixtures.transaction(
                                "old",
                                LocalDate.parse("2026-05-01"),
                                new BigDecimal("999.00"),
                                "OTHER"),
                        excluded(
                                "transfer",
                                LocalDate.parse("2026-06-04"),
                                new BigDecimal("50.00")));

        MonthlyStats stats =
                newService().buildMonthlyStats(YearMonth.parse("2026-06"), transactions);

        assertThat(stats.income()).isEqualByComparingTo("4000.00");
        assertThat(stats.spending()).isEqualByComparingTo("1805.25");
        assertThat(stats.netCashFlow()).isEqualByComparingTo("2194.75");
        assertThat(stats.transactionCount()).isEqualTo(3);
    }

    @Test
    void buildCategoryStatsGroupsSpendingByEffectiveCategory() {
        TransactionDO groceries =
                TestFixtures.transaction(
                        "groceries",
                        LocalDate.parse("2026-06-01"),
                        new BigDecimal("80.00"),
                        "GENERAL_MERCHANDISE");
        TransactionDO restaurants =
                TestFixtures.transaction(
                        "restaurant",
                        LocalDate.parse("2026-06-02"),
                        new BigDecimal("40.00"),
                        "FOOD_AND_DRINK");
        TransactionDO localOverride =
                new TransactionDO(
                        "coffee",
                        "item-1",
                        "acc-checking",
                        "Main Checking",
                        LocalDate.parse("2026-06-03"),
                        "Coffee",
                        "Coffee",
                        new BigDecimal("5.00"),
                        "FOOD_AND_DRINK",
                        "FOOD_AND_DRINK_COFFEE",
                        "Treats",
                        null,
                        false,
                        false,
                        "in store");
        TransactionDO assignedGroceries =
                TestFixtures.transaction(
                                "assigned",
                                LocalDate.parse("2026-06-04"),
                                new BigDecimal("90.00"),
                                "GENERAL_MERCHANDISE")
                        .withLocalCategoryId("groceries");

        List<CategoryStats> stats =
                newService()
                        .buildCategoryStats(
                                YearMonth.parse("2026-06"),
                                List.of(groceries, restaurants, localOverride, assignedGroceries));

        assertThat(stats)
                .extracting(CategoryStats::category)
                .containsExactly("Groceries", "GENERAL_MERCHANDISE", "FOOD_AND_DRINK", "Treats");
        assertThat(stats.get(0).amount()).isEqualByComparingTo("90.00");
    }

    private TransactionDO excluded(String id, LocalDate date, BigDecimal amount) {
        TransactionDO transaction = TestFixtures.transaction(id, date, amount, "TRANSFER");
        return new TransactionDO(
                transaction.transactionId(),
                transaction.plaidItemId(),
                transaction.accountId(),
                transaction.accountName(),
                transaction.date(),
                transaction.name(),
                transaction.merchantName(),
                transaction.amount(),
                transaction.primaryCategory(),
                transaction.detailedCategory(),
                transaction.localCategory(),
                transaction.localCategoryId(),
                transaction.pending(),
                true,
                transaction.paymentChannel());
    }

    private StatsAggregationService newService() {
        CategoryConverter converter = new CategoryConverter();
        return new StatsAggregationService(
                new CategoryService(
                        new CategoryCsvRepository(TestFixtures.properties(dataDirectory), converter),
                        converter));
    }
}
