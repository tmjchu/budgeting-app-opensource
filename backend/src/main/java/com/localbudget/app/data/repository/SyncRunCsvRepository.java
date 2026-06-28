package com.localbudget.app.data.repository;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.data.model.SyncRunCsvRecord;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class SyncRunCsvRepository extends CsvSupport {

    private static final String FILE_NAME = "sync_runs.csv";
    private static final String[] HEADERS = {
            "sync_id", "started_at", "finished_at", "status", "transactions_added",
            "transactions_updated", "balance_snapshots_added", "error_message"
    };

    public SyncRunCsvRepository(BudgetAppProperties properties) {
        super(properties);
    }

    public List<SyncRunCsvRecord> findAll() {
        return readRecords(FILE_NAME, HEADERS).stream()
                .map(record -> new SyncRunCsvRecord(
                        value(record, "sync_id"),
                        value(record, "started_at"),
                        value(record, "finished_at"),
                        value(record, "status"),
                        value(record, "transactions_added"),
                        value(record, "transactions_updated"),
                        value(record, "balance_snapshots_added"),
                        value(record, "error_message")))
                .toList();
    }

    public void upsert(SyncRunCsvRecord syncRun) {
        Map<String, SyncRunCsvRecord> merged = new LinkedHashMap<>();
        for (SyncRunCsvRecord existing : findAll()) {
            merged.put(existing.syncId(), existing);
        }
        merged.put(syncRun.syncId(), syncRun);
        writeAll(new ArrayList<>(merged.values()));
    }

    private void writeAll(List<SyncRunCsvRecord> syncRuns) {
        syncRuns.sort(Comparator.comparing(SyncRunCsvRecord::startedAt));
        writeRows(FILE_NAME, HEADERS, syncRuns.stream()
                .map(record -> List.of(
                        value(record.syncId()),
                        value(record.startedAt()),
                        value(record.finishedAt()),
                        value(record.status()),
                        value(record.transactionsAdded()),
                        value(record.transactionsUpdated()),
                        value(record.balanceSnapshotsAdded()),
                        value(record.errorMessage())))
                .toList());
    }
}
