package com.localbudget.app.data.repository;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.data.model.BalanceSnapshotCsvRecord;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class BalanceSnapshotCsvRepository extends CsvSupport {

    private static final String FILE_NAME = "balance_snapshots.csv";
    private static final String[] HEADERS = {
            "snapshot_id", "synced_at", "plaid_item_id", "account_id", "account_name", "account_mask",
            "account_type", "account_subtype", "current_balance", "available_balance",
            "iso_currency_code", "unofficial_currency_code"
    };

    public BalanceSnapshotCsvRepository(BudgetAppProperties properties) {
        super(properties);
    }

    public List<BalanceSnapshotCsvRecord> findAll() {
        return readRecords(FILE_NAME, HEADERS).stream()
                .map(record -> new BalanceSnapshotCsvRecord(
                        value(record, "snapshot_id"),
                        value(record, "synced_at"),
                        value(record, "plaid_item_id"),
                        value(record, "account_id"),
                        value(record, "account_name"),
                        value(record, "account_mask"),
                        value(record, "account_type"),
                        value(record, "account_subtype"),
                        value(record, "current_balance"),
                        value(record, "available_balance"),
                        value(record, "iso_currency_code"),
                        value(record, "unofficial_currency_code")))
                .toList();
    }

    public void appendAll(List<BalanceSnapshotCsvRecord> snapshots) {
        List<BalanceSnapshotCsvRecord> all = new ArrayList<>(findAll());
        all.addAll(snapshots);
        all.sort(Comparator.comparing(BalanceSnapshotCsvRecord::syncedAt)
                .thenComparing(BalanceSnapshotCsvRecord::accountId));
        writeRows(FILE_NAME, HEADERS, all.stream()
                .map(record -> List.of(
                        value(record.snapshotId()),
                        value(record.syncedAt()),
                        value(record.plaidItemId()),
                        value(record.accountId()),
                        value(record.accountName()),
                        value(record.accountMask()),
                        value(record.accountType()),
                        value(record.accountSubtype()),
                        value(record.currentBalance()),
                        value(record.availableBalance()),
                        value(record.isoCurrencyCode()),
                        value(record.unofficialCurrencyCode())))
                .toList());
    }
}
