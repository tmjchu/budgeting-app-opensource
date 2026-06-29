package com.localbudget.app.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.converter.TransactionConverter;
import com.localbudget.app.data.model.TransactionCsvRecord;
import com.localbudget.app.data.repository.TransactionCsvRepository;
import com.localbudget.app.domain.model.TransactionDO;
import com.localbudget.app.domain.model.result.TransactionMergeResult;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionMergeServiceTest {

    @Mock private TransactionCsvRepository repository;

    private final TransactionConverter converter = new TransactionConverter();

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

        TransactionMergeService service = new TransactionMergeService(repository, converter);
        TransactionMergeResult result =
                service.mergeIntoLocalStore(List.of(fetchedUpdated, fetchedNew));

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
        assertThat(savedUpdated.excluded()).isTrue();
    }
}
