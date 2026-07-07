package com.localbudget.app.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.converter.CategoryConverter;
import com.localbudget.app.data.repository.CategoryCsvRepository;
import com.localbudget.app.domain.model.TransactionDO;
import com.localbudget.app.domain.model.command.TransactionQueryCommand;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionQueryServiceTest {

    @TempDir Path dataDirectory;

    @Mock private TransactionMergeService transactionMergeService;

    @Test
    void findFiltersByMonthAccountAndCustomCategoryDisplayNameThenSortsNewestFirst() {
        TransactionDO groceries =
                TestFixtures.transaction(
                                "groceries",
                                LocalDate.parse("2026-06-02"),
                                new BigDecimal("20.00"),
                                "GENERAL_MERCHANDISE")
                        .withLocalCategoryId("groceries");
        TransactionDO olderGroceries =
                TestFixtures.transaction(
                                "older",
                                LocalDate.parse("2026-06-01"),
                                new BigDecimal("10.00"),
                                "GENERAL_MERCHANDISE")
                        .withLocalCategoryId("groceries");
        TransactionDO dining =
                TestFixtures.transaction(
                                "dining",
                                LocalDate.parse("2026-06-03"),
                                new BigDecimal("30.00"),
                                "FOOD_AND_DRINK")
                        .withLocalCategoryId("dining-drinks");
        TransactionDO otherMonth =
                TestFixtures.transaction(
                                "old-month",
                                LocalDate.parse("2026-05-01"),
                                new BigDecimal("40.00"),
                                "GENERAL_MERCHANDISE")
                        .withLocalCategoryId("groceries");
        when(transactionMergeService.findAll())
                .thenReturn(List.of(dining, otherMonth, groceries, olderGroceries));

        List<TransactionDO> result =
                newService()
                        .find(
                                new TransactionQueryCommand(
                                        YearMonth.parse("2026-06"),
                                        null,
                                        null,
                                        "acc-checking",
                                        "Groceries"));

        assertThat(result)
                .extracting(TransactionDO::transactionId)
                .containsExactly("groceries", "older");
    }

    @Test
    void findUsesExplicitDateRangeAndAllowsBlankFilters() {
        TransactionDO included =
                TestFixtures.transaction(
                        "included",
                        LocalDate.parse("2026-06-15"),
                        new BigDecimal("20.00"),
                        "GENERAL_MERCHANDISE");
        TransactionDO before =
                TestFixtures.transaction(
                        "before",
                        LocalDate.parse("2026-06-01"),
                        new BigDecimal("20.00"),
                        "GENERAL_MERCHANDISE");
        when(transactionMergeService.findAll()).thenReturn(List.of(before, included));

        List<TransactionDO> result =
                newService()
                        .find(
                                new TransactionQueryCommand(
                                        null,
                                        LocalDate.parse("2026-06-10"),
                                        LocalDate.parse("2026-06-20"),
                                        "",
                                        ""));

        assertThat(result).extracting(TransactionDO::transactionId).containsExactly("included");
    }

    @Test
    void findAllDelegatesToMergeService() {
        TransactionDO transaction =
                TestFixtures.transaction(
                        "txn-1",
                        LocalDate.parse("2026-06-01"),
                        new BigDecimal("20.00"),
                        "GENERAL_MERCHANDISE");
        when(transactionMergeService.findAll()).thenReturn(List.of(transaction));

        assertThat(newService().findAll()).containsExactly(transaction);
    }

    private TransactionQueryService newService() {
        CategoryConverter converter = new CategoryConverter();
        return new TransactionQueryService(
                transactionMergeService,
                new CategoryService(
                        new CategoryCsvRepository(TestFixtures.properties(dataDirectory), converter),
                        converter));
    }
}
