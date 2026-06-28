package com.localbudget.app.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.converter.BalanceSnapshotConverter;
import com.localbudget.app.data.model.BalanceSnapshotCsvRecord;
import com.localbudget.app.data.repository.BalanceSnapshotCsvRepository;
import com.localbudget.app.domain.model.Account;
import com.localbudget.app.domain.model.BalanceSnapshot;
import com.localbudget.app.gateway.plaid.api.PlaidGateway;
import com.localbudget.app.gateway.plaid.model.PlaidBalance;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class BalanceSnapshotServiceTest {

    @Mock
    private PlaidGateway plaidGateway;
    @Mock
    private AccountService accountService;
    @Mock
    private BalanceSnapshotCsvRepository repository;

    @Test
    void captureCurrentBalancesFetchesOnlyTrackedAccountsAndAppendsSnapshotRows() {
        Account account = TestFixtures.checkingAccount();
        when(accountService.findTrackedByPlaidItemId("item-1")).thenReturn(List.of(account));
        when(plaidGateway.fetchBalances(TestFixtures.plaidItem(), List.of(account)))
                .thenReturn(List.of(new PlaidBalance(
                        "item-1",
                        "acc-checking",
                        "Main Checking",
                        "1234",
                        "depository",
                        "checking",
                        new BigDecimal("200.00"),
                        new BigDecimal("180.00"),
                        "USD",
                        null)));

        Clock clock = Clock.fixed(Instant.parse("2026-06-01T12:00:00Z"), ZoneOffset.UTC);
        BalanceSnapshotService service = new BalanceSnapshotService(
                plaidGateway,
                accountService,
                repository,
                new BalanceSnapshotConverter(),
                clock);

        List<BalanceSnapshot> snapshots = service.captureCurrentBalances(List.of(TestFixtures.plaidItem()));

        assertThat(snapshots).hasSize(1);
        assertThat(snapshots.get(0).snapshotId()).isEqualTo("2026-06-01T12:00:00Z_acc-checking");
        assertThat(snapshots.get(0).currentBalance()).isEqualByComparingTo("200.00");
        ArgumentCaptor<List<BalanceSnapshotCsvRecord>> captor = ArgumentCaptor.forClass(List.class);
        verify(repository).appendAll(captor.capture());
        assertThat(captor.getValue()).hasSize(1);
        assertThat(captor.getValue().get(0).syncedAt()).isEqualTo("2026-06-01T12:00:00Z");
    }
}
