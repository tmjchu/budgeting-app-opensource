package com.localbudget.app.data.repository;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.data.model.TransactionCsvRecord;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.springframework.stereotype.Repository;

@Repository
public class TransactionCsvRepository extends CsvSupport {

    private static final String FILE_NAME = "transactions.csv";
    private static final String[] HEADERS = {
            "transaction_id", "plaid_item_id", "account_id", "account_name", "date", "name",
            "merchant_name", "amount", "primary_category", "detailed_category", "local_category",
            "pending", "excluded", "payment_channel"
    };

    public TransactionCsvRepository(BudgetAppProperties properties) {
        super(properties);
    }

    public List<TransactionCsvRecord> findAll() {
        return readRecords(FILE_NAME, HEADERS).stream()
                .map(record -> new TransactionCsvRecord(
                        value(record, "transaction_id"),
                        value(record, "plaid_item_id"),
                        value(record, "account_id"),
                        value(record, "account_name"),
                        value(record, "date"),
                        value(record, "name"),
                        value(record, "merchant_name"),
                        value(record, "amount"),
                        value(record, "primary_category"),
                        value(record, "detailed_category"),
                        value(record, "local_category"),
                        value(record, "pending"),
                        value(record, "excluded"),
                        value(record, "payment_channel")))
                .toList();
    }

    public Optional<TransactionCsvRecord> findById(String transactionId) {
        return findAll().stream()
                .filter(record -> transactionId.equals(record.transactionId()))
                .findFirst();
    }

    public void writeAll(List<TransactionCsvRecord> transactions) {
        List<TransactionCsvRecord> sorted = new ArrayList<>(transactions);
        sorted.sort(Comparator.comparing(TransactionCsvRecord::date).reversed()
                .thenComparing(TransactionCsvRecord::transactionId));
        writeRows(FILE_NAME, HEADERS, sorted.stream()
                .map(record -> List.of(
                        value(record.transactionId()),
                        value(record.plaidItemId()),
                        value(record.accountId()),
                        value(record.accountName()),
                        value(record.date()),
                        value(record.name()),
                        value(record.merchantName()),
                        value(record.amount()),
                        value(record.primaryCategory()),
                        value(record.detailedCategory()),
                        value(record.localCategory()),
                        value(record.pending()),
                        value(record.excluded()),
                        value(record.paymentChannel())))
                .toList());
    }

    public void upsertAll(List<TransactionCsvRecord> transactions) {
        Map<String, TransactionCsvRecord> merged = new LinkedHashMap<>();
        for (TransactionCsvRecord existing : findAll()) {
            merged.put(existing.transactionId(), existing);
        }
        for (TransactionCsvRecord transaction : transactions) {
            merged.put(transaction.transactionId(), transaction);
        }
        writeAll(new ArrayList<>(merged.values()));
    }
}
