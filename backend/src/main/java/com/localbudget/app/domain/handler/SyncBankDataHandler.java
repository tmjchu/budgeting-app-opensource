package com.localbudget.app.domain.handler;

import com.localbudget.app.domain.model.BalanceSnapshot;
import com.localbudget.app.domain.model.PlaidItem;
import com.localbudget.app.domain.model.SyncRun;
import com.localbudget.app.domain.model.TransactionDO;
import com.localbudget.app.domain.model.result.SyncResult;
import com.localbudget.app.domain.model.result.TransactionMergeResult;
import com.localbudget.app.domain.service.BalanceSnapshotService;
import com.localbudget.app.domain.service.PlaidConnectionService;
import com.localbudget.app.domain.service.SyncRunService;
import com.localbudget.app.domain.service.TransactionFetchService;
import com.localbudget.app.domain.service.TransactionMergeService;
import com.localbudget.app.domain.service.TransactionRuleService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class SyncBankDataHandler {

    private static final int DEFAULT_LOOKBACK_DAYS = 365;

    private final SyncRunService syncRunService;
    private final PlaidConnectionService plaidConnectionService;
    private final TransactionFetchService transactionFetchService;
    private final TransactionRuleService transactionRuleService;
    private final TransactionMergeService transactionMergeService;
    private final BalanceSnapshotService balanceSnapshotService;
    private final Clock clock;

    public SyncBankDataHandler(
            SyncRunService syncRunService,
            PlaidConnectionService plaidConnectionService,
            TransactionFetchService transactionFetchService,
            TransactionRuleService transactionRuleService,
            TransactionMergeService transactionMergeService,
            BalanceSnapshotService balanceSnapshotService,
            Clock clock) {
        this.syncRunService = syncRunService;
        this.plaidConnectionService = plaidConnectionService;
        this.transactionFetchService = transactionFetchService;
        this.transactionRuleService = transactionRuleService;
        this.transactionMergeService = transactionMergeService;
        this.balanceSnapshotService = balanceSnapshotService;
        this.clock = clock;
    }

    public SyncResult handle() {
        SyncRun syncRun = syncRunService.start();
        try {
            List<PlaidItem> connectedItems = plaidConnectionService.findConnectedItems();

            LocalDate endDate = LocalDate.now(clock);
            LocalDate startDate = endDate.minusDays(DEFAULT_LOOKBACK_DAYS);

            List<TransactionDO> fetchedTransactions =
                    transactionFetchService.fetchTransactions(connectedItems, startDate, endDate);
            List<TransactionDO> normalizedTransactions =
                    transactionRuleService.applyRules(fetchedTransactions);
            TransactionMergeResult mergeResult =
                    transactionMergeService.mergeIntoLocalStore(normalizedTransactions);
            List<BalanceSnapshot> snapshots =
                    balanceSnapshotService.captureCurrentBalances(connectedItems);

            SyncRun completed = syncRunService.markSuccess(syncRun, mergeResult, snapshots.size());
            return new SyncResult(completed, mergeResult, snapshots);
        } catch (RuntimeException ex) {
            syncRunService.markFailed(syncRun, ex);
            throw ex;
        }
    }
}
