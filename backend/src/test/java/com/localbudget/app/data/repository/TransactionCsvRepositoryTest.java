package com.localbudget.app.data.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.data.model.TransactionCsvRecord;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class TransactionCsvRepositoryTest {

    @TempDir Path dataDirectory;

    @Test
    void writeAllSortsByDateDescendingAndRoundTripsQuotedText() throws IOException {
        TransactionCsvRepository repository =
                new TransactionCsvRepository(TestFixtures.properties(dataDirectory));

        repository.writeAll(
                List.of(
                        transaction("txn-old", "2026-01-01", "Coffee, Inc."),
                        transaction("txn-new", "2026-02-01", "Market \"Special\"")));

        assertThat(repository.findAll())
                .extracting(TransactionCsvRecord::transactionId)
                .containsExactly("txn-new", "txn-old");
        assertThat(repository.findById("txn-new"))
                .get()
                .extracting(TransactionCsvRecord::merchantName)
                .isEqualTo("Market \"Special\"");
        assertThat(Files.readString(dataDirectory.resolve("transactions.csv")))
                .startsWith(
                        "transaction_id,plaid_item_id,account_id,account_name,date,name,merchant_name,amount,primary_category,detailed_category,local_category,pending,excluded,payment_channel,local_category_id,custom_name,custom_date");
        assertThat(repository.findById("txn-new"))
                .get()
                .satisfies(
                        record -> {
                            assertThat(record.customName()).isEqualTo("Custom txn-new");
                            assertThat(record.customDate()).isEqualTo("2026-03-01");
                        });
    }

    @Test
    void readsOldTransactionCsvWithoutLocalCategoryId() throws Exception {
        Files.writeString(
                dataDirectory.resolve("transactions.csv"),
                """
                transaction_id,plaid_item_id,account_id,account_name,date,name,merchant_name,amount,primary_category,detailed_category,local_category,pending,excluded,payment_channel
                txn-old,item-1,acc-1,Checking,2026-01-01,Name,Merchant,12.34,FOOD_AND_DRINK,FOOD_AND_DRINK_COFFEE,Dining & Drinks,false,false,in store
                """);
        TransactionCsvRepository repository =
                new TransactionCsvRepository(TestFixtures.properties(dataDirectory));

        TransactionCsvRecord record = repository.findAll().getFirst();

        assertThat(record.localCategory()).isEqualTo("Dining & Drinks");
        assertThat(record.pending()).isEqualTo("false");
        assertThat(record.excluded()).isEqualTo("false");
        assertThat(record.paymentChannel()).isEqualTo("in store");
        assertThat(record.localCategoryId()).isNull();
        assertThat(record.customName()).isNull();
        assertThat(record.customDate()).isNull();
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
                "in store",
                null,
                "Custom " + id,
                id.equals("txn-new") ? "2026-03-01" : null);
    }
}
