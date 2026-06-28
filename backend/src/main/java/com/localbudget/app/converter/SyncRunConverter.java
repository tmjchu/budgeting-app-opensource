package com.localbudget.app.converter;

import com.localbudget.app.api.model.response.SyncResponse;
import com.localbudget.app.data.model.SyncRunCsvRecord;
import com.localbudget.app.domain.model.SyncRun;
import com.localbudget.app.domain.model.SyncStatus;
import com.localbudget.app.domain.model.result.SyncResult;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class SyncRunConverter {

    public SyncRun fromCsv(SyncRunCsvRecord record) {
        return new SyncRun(
                record.syncId(),
                Instant.parse(record.startedAt()),
                record.finishedAt() == null ? null : Instant.parse(record.finishedAt()),
                SyncStatus.valueOf(record.status()),
                parseInt(record.transactionsAdded()),
                parseInt(record.transactionsUpdated()),
                parseInt(record.balanceSnapshotsAdded()),
                record.errorMessage());
    }

    public SyncRunCsvRecord toCsv(SyncRun syncRun) {
        return new SyncRunCsvRecord(
                syncRun.syncId(),
                syncRun.startedAt().toString(),
                syncRun.finishedAt() == null ? null : syncRun.finishedAt().toString(),
                syncRun.status().name(),
                String.valueOf(syncRun.transactionsAdded()),
                String.valueOf(syncRun.transactionsUpdated()),
                String.valueOf(syncRun.balanceSnapshotsAdded()),
                syncRun.errorMessage());
    }

    public SyncResponse toResponse(SyncResult result) {
        SyncRun syncRun = result.syncRun();
        return new SyncResponse(
                syncRun.syncId(),
                syncRun.status().name(),
                syncRun.startedAt(),
                syncRun.finishedAt(),
                syncRun.transactionsAdded(),
                syncRun.transactionsUpdated(),
                syncRun.balanceSnapshotsAdded(),
                syncRun.errorMessage());
    }

    private static int parseInt(String value) {
        return value == null || value.isBlank() ? 0 : Integer.parseInt(value);
    }
}
