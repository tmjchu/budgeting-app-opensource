package com.localbudget.app.domain.handler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.domain.model.BalanceSnapshot;
import com.localbudget.app.domain.model.PlaidItem;
import com.localbudget.app.domain.model.SyncRun;
import com.localbudget.app.domain.model.SyncStatus;
import com.localbudget.app.domain.model.Transaction;
import com.localbudget.app.domain.model.result.SyncResult;
import com.localbudget.app.domain.model.result.TransactionMergeResult;
import com.localbudget.app.domain.service.BalanceSnapshotService;
import com.localbudget.app.domain.service.PlaidConnectionService;
import com.localbudget.app.domain.service.SyncRunService;
import com.localbudget.app.domain.service.TransactionFetchService;
import com.localbudget.app.domain.service.TransactionMergeService;
import com.localbudget.app.domain.service.TransactionRuleService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SyncBankDataHandlerTest {

    @Mock private SyncRunService syncRunService;
    @Mock private PlaidConnectionService plaidConnectionService;
    @Mock private TransactionFetchService transactionFetchService;
    @Mock private TransactionRuleService transactionRuleService;
    @Mock private TransactionMergeService transactionMergeService;
    @Mock private BalanceSnapshotService balanceSnapshotService;

    private final Clock clock = Clock.fixed(Instant.parse("2026-06-28T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void handleCoordinatesTransactionSyncAndBalanceCapture() {
        SyncRun started =
                new SyncRun(
                        "sync-1",
                        Instant.parse("2026-06-28T00:00:00Z"),
                        null,
                        SyncStatus.RUNNING,
                        0,
                        0,
                        0,
                        null);
        SyncRun completed =
                new SyncRun(
                        "sync-1",
                        started.startedAt(),
                        Instant.parse("2026-06-28T00:01:00Z"),
                        SyncStatus.SUCCESS,
                        1,
                        0,
                        1,
                        null);
        PlaidItem plaidItem = TestFixtures.plaidItem();
        Transaction fetched =
                TestFixtures.transaction(
                        "txn-1",
                        LocalDate.parse("2026-06-27"),
                        new BigDecimal("10.00"),
                        "FOOD_AND_DRINK");
        TransactionMergeResult mergeResult = new TransactionMergeResult(1, 0, 0);
        BalanceSnapshot snapshot =
                new BalanceSnapshot(
                        "snap-1",
                        Instant.parse("2026-06-28T00:00:00Z"),
                        "item-1",
                        "acc-checking",
                        "Main Checking",
                        "1234",
                        "depository",
                        "checking",
                        new BigDecimal("100.00"),
                        new BigDecimal("90.00"),
                        "USD",
                        null);

        when(syncRunService.start()).thenReturn(started);
        when(plaidConnectionService.findConnectedItems()).thenReturn(List.of(plaidItem));
        when(transactionFetchService.fetchTransactions(
                        List.of(plaidItem),
                        LocalDate.parse("2025-06-28"),
                        LocalDate.parse("2026-06-28")))
                .thenReturn(List.of(fetched));
        when(transactionRuleService.applyRules(List.of(fetched))).thenReturn(List.of(fetched));
        when(transactionMergeService.mergeIntoLocalStore(List.of(fetched))).thenReturn(mergeResult);
        when(balanceSnapshotService.captureCurrentBalances(List.of(plaidItem)))
                .thenReturn(List.of(snapshot));
        when(syncRunService.markSuccess(started, mergeResult, 1)).thenReturn(completed);

        SyncResult result = handler().handle();

        assertThat(result.syncRun()).isEqualTo(completed);
        assertThat(result.transactionMergeResult()).isEqualTo(mergeResult);
        assertThat(result.balanceSnapshots()).containsExactly(snapshot);
    }

    @Test
    void handleMarksSyncRunFailedWhenAnyStepFails() {
        SyncRun started =
                new SyncRun(
                        "sync-1",
                        Instant.parse("2026-06-28T00:00:00Z"),
                        null,
                        SyncStatus.RUNNING,
                        0,
                        0,
                        0,
                        null);
        RuntimeException failure = new RuntimeException("Plaid unavailable");
        when(syncRunService.start()).thenReturn(started);
        when(plaidConnectionService.findConnectedItems()).thenThrow(failure);

        assertThatThrownBy(() -> handler().handle()).isSameAs(failure);
        verify(syncRunService).markFailed(started, failure);
    }

    private SyncBankDataHandler handler() {
        return new SyncBankDataHandler(
                syncRunService,
                plaidConnectionService,
                transactionFetchService,
                transactionRuleService,
                transactionMergeService,
                balanceSnapshotService,
                clock);
    }
}
