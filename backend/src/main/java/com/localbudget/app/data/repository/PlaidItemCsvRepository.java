package com.localbudget.app.data.repository;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.data.model.PlaidItemCsvRecord;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class PlaidItemCsvRepository extends CsvSupport {

    private static final String FILE_NAME = "plaid_items.csv";
    private static final String[] HEADERS = {
            "plaid_item_id", "access_token", "institution_name", "created_at"
    };

    public PlaidItemCsvRepository(BudgetAppProperties properties) {
        super(properties);
    }

    public List<PlaidItemCsvRecord> findAll() {
        return readRecords(FILE_NAME, HEADERS).stream()
                .map(record -> new PlaidItemCsvRecord(
                        value(record, "plaid_item_id"),
                        value(record, "access_token"),
                        value(record, "institution_name"),
                        value(record, "created_at")))
                .toList();
    }

    public void upsert(PlaidItemCsvRecord item) {
        Map<String, PlaidItemCsvRecord> merged = new LinkedHashMap<>();
        for (PlaidItemCsvRecord existing : findAll()) {
            merged.put(existing.plaidItemId(), existing);
        }
        merged.put(item.plaidItemId(), item);
        writeAll(new ArrayList<>(merged.values()));
    }

    private void writeAll(List<PlaidItemCsvRecord> items) {
        items.sort(Comparator.comparing(PlaidItemCsvRecord::createdAt));
        writeRows(FILE_NAME, HEADERS, items.stream()
                .map(record -> List.of(
                        value(record.plaidItemId()),
                        value(record.accessToken()),
                        value(record.institutionName()),
                        value(record.createdAt())))
                .toList());
    }
}
