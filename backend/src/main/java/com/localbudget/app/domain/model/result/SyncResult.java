package com.localbudget.app.domain.model.result;

import com.localbudget.app.domain.model.BalanceSnapshot;
import com.localbudget.app.domain.model.SyncRun;
import java.util.List;

public record SyncResult(
        SyncRun syncRun,
        TransactionMergeResult transactionMergeResult,
        List<BalanceSnapshot> balanceSnapshots) {}
