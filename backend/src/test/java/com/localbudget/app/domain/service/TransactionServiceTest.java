package com.localbudget.app.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.converter.TransactionConverter;
import com.localbudget.app.data.model.TransactionCsvRecord;
import com.localbudget.app.data.repository.TransactionCsvRepository;
import com.localbudget.app.domain.model.TransactionDO;
import com.localbudget.app.domain.model.TransactionView;
import com.localbudget.app.domain.model.command.TransactionQueryCommand;
import com.localbudget.app.domain.model.result.TransactionMergeResult;
import com.localbudget.app.domain.service.helper.TransactionServiceHelper;
import com.localbudget.app.gateway.plaid.api.PlaidGateway;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionServiceTest {

    @Mock private PlaidGateway plaidGateway;
    @Mock private TransactionCsvRepository repository;

    private final TransactionConverter converter = new TransactionConverter();
    private final TransactionServiceHelper helper = new TransactionServiceHelper();

    @Test
    void mergeIntoLocalStoreAddsNewTransactionsAndPreservesExistingLocalEdits() {
        TransactionDO existing =
                new TransactionDO(
                        "txn-1",
                        "item-1",
                        "acc-checking",
                        "Main Checking",
                        LocalDate.parse("2026-06-01"),
                        "Old Name",
                        "Old Merchant",
                        new BigDecimal("10.00"),
                        "FOOD_AND_DRINK",
                        "FOOD_AND_DRINK_COFFEE",
                        "Coffee",
                        null,
                        true,
                        true,
                        "in store");
        TransactionDO fetchedUpdated =
                TestFixtures.transaction(
                        "txn-1",
                        LocalDate.parse("2026-06-02"),
                        new BigDecimal("12.00"),
                        "FOOD_AND_DRINK");
        TransactionDO fetchedNew =
                TestFixtures.transaction(
                        "txn-2",
                        LocalDate.parse("2026-06-03"),
                        new BigDecimal("20.00"),
                        "GENERAL_MERCHANDISE");
        when(repository.findAll()).thenReturn(List.of(converter.toCsv(existing)));

        TransactionMergeResult result =
                newService().mergeIntoLocalStore(List.of(fetchedUpdated, fetchedNew));

        assertThat(result.added()).isEqualTo(1);
        assertThat(result.updated()).isEqualTo(1);
        assertThat(result.unchanged()).isZero();

        ArgumentCaptor<List<TransactionCsvRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).writeAll(captor.capture());
        List<TransactionDO> saved = captor.getValue().stream().map(converter::fromCsv).toList();
        assertThat(saved).hasSize(2);
        TransactionDO savedUpdated =
                saved.stream()
                        .filter(transaction -> transaction.transactionId().equals("txn-1"))
                        .findFirst()
                        .orElseThrow();
        assertThat(savedUpdated.date()).isEqualTo(LocalDate.parse("2026-06-02"));
        assertThat(savedUpdated.localCategory()).isEqualTo("Coffee");
        assertThat(savedUpdated.localCategoryId()).isNull();
        assertThat(savedUpdated.excluded()).isTrue();
    }

    @Test
    void mergeIntoLocalStoreAssignsDefaultCategoryIdAndPreservesExistingAssignment() {
        TransactionDO existing =
                TestFixtures.transaction(
                                "txn-1",
                                LocalDate.parse("2026-06-01"),
                                new BigDecimal("10.00"),
                                "FOOD_AND_DRINK")
                        .withLocalCategoryId("groceries");
        TransactionDO fetchedUpdated =
                TestFixtures.transaction(
                        "txn-1",
                        LocalDate.parse("2026-06-02"),
                        new BigDecimal("12.00"),
                        "FOOD_AND_DRINK");
        TransactionDO fetchedNew =
                TestFixtures.transaction(
                        "txn-2",
                        LocalDate.parse("2026-06-03"),
                        new BigDecimal("20.00"),
                        "FOOD_AND_DRINK");
        when(repository.findAll()).thenReturn(List.of(converter.toCsv(existing)));

        newService().mergeIntoLocalStore(List.of(fetchedUpdated, fetchedNew));

        ArgumentCaptor<List<TransactionCsvRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).writeAll(captor.capture());
        List<TransactionDO> saved = captor.getValue().stream().map(converter::fromCsv).toList();

        assertThat(saved)
                .filteredOn(transaction -> transaction.transactionId().equals("txn-1"))
                .singleElement()
                .extracting(TransactionDO::localCategoryId)
                .isEqualTo("groceries");
        assertThat(saved)
                .filteredOn(transaction -> transaction.transactionId().equals("txn-2"))
                .singleElement()
                .extracting(TransactionDO::localCategoryId)
                .isEqualTo("dining-drinks");
    }

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
        when(repository.findAll())
                .thenReturn(
                        List.of(dining, otherMonth, groceries, olderGroceries).stream()
                                .map(converter::toCsv)
                                .toList());

        List<TransactionView> result =
                newService()
                        .find(
                                new TransactionQueryCommand(
                                        YearMonth.parse("2026-06"),
                                        null,
                                        null,
                                        "acc-checking",
                                        "Groceries"),
                                Map.of("groceries", "Groceries", "dining-drinks", "Dining"));

        assertThat(result)
                .extracting(view -> view.transaction().transactionId())
                .containsExactly("groceries", "older");
        assertThat(result).extracting(TransactionView::categoryDisplayName)
                .containsExactly("Groceries", "Groceries");
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
        when(repository.findAll())
                .thenReturn(List.of(before, included).stream().map(converter::toCsv).toList());

        List<TransactionView> result =
                newService()
                        .find(
                                new TransactionQueryCommand(
                                        null,
                                        LocalDate.parse("2026-06-10"),
                                        LocalDate.parse("2026-06-20"),
                                        "",
                                        ""),
                                Map.of());

        assertThat(result)
                .extracting(view -> view.transaction().transactionId())
                .containsExactly("included");
    }

    @Test
    void applyRulesMarksPlaidTransfersAsExcluded() {
        TransactionDO transfer =
                TestFixtures.transaction(
                        "transfer",
                        LocalDate.parse("2026-06-01"),
                        new BigDecimal("100.00"),
                        "TRANSFER");
        TransactionDO food =
                TestFixtures.transaction(
                        "food",
                        LocalDate.parse("2026-06-02"),
                        new BigDecimal("25.00"),
                        "FOOD_AND_DRINK");

        List<TransactionDO> normalized = newService().applyRules(List.of(transfer, food));

        assertThat(normalized.get(0).excluded()).isTrue();
        assertThat(normalized.get(1).excluded()).isFalse();
    }

    @Test
    void updateLocalCategoryPersistsSelectedCategoryAndClearsLegacyCategory() {
        TransactionDO existing =
                new TransactionDO(
                        "txn-1",
                        "item-1",
                        "acc-checking",
                        "Main Checking",
                        LocalDate.parse("2026-06-01"),
                        "Old Name",
                        "Old Merchant",
                        new BigDecimal("10.00"),
                        "FOOD_AND_DRINK",
                        "FOOD_AND_DRINK_COFFEE",
                        "Legacy",
                        null,
                        false,
                        false,
                        "in store");
        when(repository.findAll()).thenReturn(List.of(converter.toCsv(existing)));

        TransactionDO updated = newService().updateLocalCategory("txn-1", "groceries");

        assertThat(updated.localCategoryId()).isEqualTo("groceries");
        assertThat(updated.localCategory()).isNull();
        ArgumentCaptor<List<TransactionCsvRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).writeAll(captor.capture());
        assertThat(captor.getValue())
                .singleElement()
                .satisfies(
                        record -> {
                            assertThat(record.localCategoryId()).isEqualTo("groceries");
                            assertThat(record.localCategory()).isNull();
                        });
    }

    @Test
    void updateLocalCategoryThrowsWhenTransactionDoesNotExist() {
        when(repository.findAll()).thenReturn(List.of());

        assertThatThrownBy(() -> newService().updateLocalCategory("missing", "groceries"))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("Transaction not found: missing");
    }

    private TransactionService newService() {
        return new TransactionService(plaidGateway, repository, converter, helper);
    }
}
