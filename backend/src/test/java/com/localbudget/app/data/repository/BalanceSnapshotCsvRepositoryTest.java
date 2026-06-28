package com.localbudget.app.data.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.data.model.BalanceSnapshotCsvRecord;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class BalanceSnapshotCsvRepositoryTest {

    @TempDir Path dataDirectory;

    @Test
    void appendAllPreservesHistoricalSnapshots() {
        BalanceSnapshotCsvRepository repository =
                new BalanceSnapshotCsvRepository(TestFixtures.properties(dataDirectory));

        repository.appendAll(List.of(snapshot("snap-1", "2026-01-01T00:00:00Z")));
        repository.appendAll(List.of(snapshot("snap-2", "2026-01-02T00:00:00Z")));

        assertThat(repository.findAll())
                .extracting(BalanceSnapshotCsvRecord::snapshotId)
                .containsExactly("snap-1", "snap-2");
    }

    private BalanceSnapshotCsvRecord snapshot(String id, String syncedAt) {
        return new BalanceSnapshotCsvRecord(
                id,
                syncedAt,
                "item-1",
                "acc-1",
                "Checking",
                "1234",
                "depository",
                "checking",
                "100.00",
                "90.00",
                "USD",
                null);
    }
}
