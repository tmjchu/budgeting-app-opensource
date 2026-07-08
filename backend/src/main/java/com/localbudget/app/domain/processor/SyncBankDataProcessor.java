package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.model.AccountDO;
import com.localbudget.app.domain.model.BalanceSnapshot;
import com.localbudget.app.domain.model.PlaidItem;
import com.localbudget.app.domain.model.SyncRun;
import com.localbudget.app.domain.model.TransactionDO;
import com.localbudget.app.domain.model.result.SyncResult;
import com.localbudget.app.domain.model.result.TransactionMergeResult;
import com.localbudget.app.domain.service.AccountService;
import com.localbudget.app.domain.service.BalanceSnapshotService;
import com.localbudget.app.domain.service.PlaidConnectionService;
import com.localbudget.app.domain.service.SyncRunService;
import com.localbudget.app.domain.service.TransactionService;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SyncBankDataProcessor {

    private static final int DEFAULT_LOOKBACK_DAYS = 365;

    private final SyncRunService syncRunService;
    private final PlaidConnectionService plaidConnectionService;
    private final AccountService accountService;
    private final TransactionService transactionService;
    private final BalanceSnapshotService balanceSnapshotService;
    private final Clock clock;

    public SyncResult process() {
        SyncRun syncRun = syncRunService.start();
        try {
            List<PlaidItem> connectedItems = plaidConnectionService.findConnectedItems();
            Map<String, List<AccountDO>> trackedAccountsByPlaidItemId =
                    trackedAccountsByPlaidItemId(connectedItems);

            LocalDate endDate = LocalDate.now(clock);
            LocalDate startDate = endDate.minusDays(DEFAULT_LOOKBACK_DAYS);

            List<TransactionDO> fetchedTransactions =
                    transactionService.fetchTransactions(
                            connectedItems,
                            trackedAccountsByPlaidItemId,
                            startDate,
                            endDate);
            List<TransactionDO> normalizedTransactions =
                    transactionService.applyRules(fetchedTransactions);
            TransactionMergeResult mergeResult =
                    transactionService.mergeIntoLocalStore(normalizedTransactions);
            List<BalanceSnapshot> snapshots =
                    balanceSnapshotService.captureCurrentBalances(
                            connectedItems, trackedAccountsByPlaidItemId);

            SyncRun completed = syncRunService.markSuccess(syncRun, mergeResult, snapshots.size());
            return new SyncResult(completed, mergeResult, snapshots);
        } catch (RuntimeException ex) {
            syncRunService.markFailed(syncRun, ex);
            throw ex;
        }
    }

    private Map<String, List<AccountDO>> trackedAccountsByPlaidItemId(List<PlaidItem> plaidItems) {
        return plaidItems.stream()
                .map(PlaidItem::plaidItemId)
                .collect(
                        Collectors.toMap(
                                Function.identity(), accountService::findTrackedByPlaidItemId));
    }
}
