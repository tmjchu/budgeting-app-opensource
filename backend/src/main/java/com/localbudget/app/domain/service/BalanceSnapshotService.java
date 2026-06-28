package com.localbudget.app.domain.service;

import com.localbudget.app.converter.BalanceSnapshotConverter;
import com.localbudget.app.data.repository.BalanceSnapshotCsvRepository;
import com.localbudget.app.domain.model.Account;
import com.localbudget.app.domain.model.BalanceSnapshot;
import com.localbudget.app.domain.model.PlaidItem;
import com.localbudget.app.gateway.plaid.api.PlaidGateway;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class BalanceSnapshotService {

    private final PlaidGateway plaidGateway;
    private final AccountService accountService;
    private final BalanceSnapshotCsvRepository balanceSnapshotRepository;
    private final BalanceSnapshotConverter balanceSnapshotConverter;
    private final Clock clock;

    public BalanceSnapshotService(
            PlaidGateway plaidGateway,
            AccountService accountService,
            BalanceSnapshotCsvRepository balanceSnapshotRepository,
            BalanceSnapshotConverter balanceSnapshotConverter,
            Clock clock) {
        this.plaidGateway = plaidGateway;
        this.accountService = accountService;
        this.balanceSnapshotRepository = balanceSnapshotRepository;
        this.balanceSnapshotConverter = balanceSnapshotConverter;
        this.clock = clock;
    }

    public List<BalanceSnapshot> findAll() {
        return balanceSnapshotRepository.findAll().stream()
                .map(balanceSnapshotConverter::fromCsv)
                .toList();
    }

    public List<BalanceSnapshot> captureCurrentBalances(List<PlaidItem> plaidItems) {
        Instant syncedAt = Instant.now(clock);
        List<BalanceSnapshot> snapshots = new ArrayList<>();
        for (PlaidItem plaidItem : plaidItems) {
            List<Account> trackedAccounts = accountService.findTrackedByPlaidItemId(plaidItem.plaidItemId());
            plaidGateway.fetchBalances(plaidItem, trackedAccounts).stream()
                    .map(balance -> balanceSnapshotConverter.fromGateway(balance, syncedAt))
                    .forEach(snapshots::add);
        }
        balanceSnapshotRepository.appendAll(snapshots.stream()
                .map(balanceSnapshotConverter::toCsv)
                .toList());
        return snapshots;
    }
}
