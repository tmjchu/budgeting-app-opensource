package com.localbudget.app.domain.service;

import com.localbudget.app.converter.SyncRunConverter;
import com.localbudget.app.data.repository.SyncRunCsvRepository;
import com.localbudget.app.domain.model.SyncRun;
import com.localbudget.app.domain.model.SyncStatus;
import com.localbudget.app.domain.model.result.TransactionMergeResult;
import java.time.Clock;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
public class SyncRunService {

    private final SyncRunCsvRepository syncRunRepository;
    private final SyncRunConverter syncRunConverter;
    private final Clock clock;

    public SyncRunService(
            SyncRunCsvRepository syncRunRepository,
            SyncRunConverter syncRunConverter,
            Clock clock) {
        this.syncRunRepository = syncRunRepository;
        this.syncRunConverter = syncRunConverter;
        this.clock = clock;
    }

    public SyncRun start() {
        SyncRun syncRun =
                new SyncRun(
                        UUID.randomUUID().toString(),
                        Instant.now(clock),
                        null,
                        SyncStatus.RUNNING,
                        0,
                        0,
                        0,
                        null);
        syncRunRepository.upsert(syncRunConverter.toCsv(syncRun));
        return syncRun;
    }

    public SyncRun markSuccess(
            SyncRun syncRun,
            TransactionMergeResult transactionMergeResult,
            int balanceSnapshotsAdded) {
        SyncRun completed =
                new SyncRun(
                        syncRun.syncId(),
                        syncRun.startedAt(),
                        Instant.now(clock),
                        SyncStatus.SUCCESS,
                        transactionMergeResult.added(),
                        transactionMergeResult.updated(),
                        balanceSnapshotsAdded,
                        null);
        syncRunRepository.upsert(syncRunConverter.toCsv(completed));
        return completed;
    }

    public SyncRun markFailed(SyncRun syncRun, Exception exception) {
        SyncRun failed =
                new SyncRun(
                        syncRun.syncId(),
                        syncRun.startedAt(),
                        Instant.now(clock),
                        SyncStatus.FAILED,
                        syncRun.transactionsAdded(),
                        syncRun.transactionsUpdated(),
                        syncRun.balanceSnapshotsAdded(),
                        exception.getMessage());
        syncRunRepository.upsert(syncRunConverter.toCsv(failed));
        return failed;
    }
}
