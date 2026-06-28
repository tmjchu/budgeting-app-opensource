package com.localbudget.app.data.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.data.model.TransactionCsvRecord;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TransactionCsvRepositoryTest {

    @TempDir
    Path dataDirectory;

    @Test
    void writeAllSortsByDateDescendingAndRoundTripsQuotedText() {
        TransactionCsvRepository repository = new TransactionCsvRepository(TestFixtures.properties(dataDirectory));

        repository.writeAll(List.of(
                transaction("txn-old", "2026-01-01", "Coffee, Inc."),
                transaction("txn-new", "2026-02-01", "Market \"Special\"")));

        assertThat(repository.findAll())
                .extracting(TransactionCsvRecord::transactionId)
                .containsExactly("txn-new", "txn-old");
        assertThat(repository.findById("txn-new")).get()
                .extracting(TransactionCsvRecord::merchantName)
                .isEqualTo("Market \"Special\"");
    }

    private TransactionCsvRecord transaction(String id, String date, String merchantName) {
        return new TransactionCsvRecord(
                id,
                "item-1",
                "acc-1",
                "Checking",
                date,
                "Name",
                merchantName,
                "12.34",
                "FOOD_AND_DRINK",
                "FOOD_AND_DRINK_COFFEE",
                null,
                "false",
                "false",
                "in store");
    }
}
