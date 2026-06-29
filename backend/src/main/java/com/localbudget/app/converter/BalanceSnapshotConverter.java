package com.localbudget.app.converter;

import com.localbudget.app.api.model.response.BalanceSnapshotResponse;
import com.localbudget.app.data.model.BalanceSnapshotCsvRecord;
import com.localbudget.app.domain.model.AccountDO;
import com.localbudget.app.domain.model.BalanceSnapshot;
import com.localbudget.app.domain.model.PlaidItem;
import com.plaid.client.model.AccountBalance;
import com.plaid.client.model.AccountBase;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class BalanceSnapshotConverter {

    public BalanceSnapshot fromPlaid(
            PlaidItem plaidItem,
            List<AccountDO> trackedAccounts,
            AccountBase accountBase,
            Instant syncedAt) {
        var accountById =
                trackedAccounts.stream()
                        .collect(Collectors.toMap(AccountDO::accountId, Function.identity()));
        String accountId = accountBase.getAccountId();
        AccountDO trackedAccount = accountById.get(accountId);
        AccountBalance balance = accountBase.getBalances();

        return new BalanceSnapshot(
                syncedAt + "_" + accountId,
                syncedAt,
                plaidItem.plaidItemId(),
                accountId,
                trackedAccount == null ? accountBase.getName() : trackedAccount.name(),
                trackedAccount == null ? accountBase.getMask() : trackedAccount.mask(),
                trackedAccount == null ? value(accountBase.getType()) : trackedAccount.type(),
                trackedAccount == null ? value(accountBase.getSubtype()) : trackedAccount.subtype(),
                balance == null ? null : amount(balance.getCurrent()),
                balance == null ? null : amount(balance.getAvailable()),
                balance == null ? null : balance.getIsoCurrencyCode(),
                balance == null ? null : balance.getUnofficialCurrencyCode());
    }

    public BalanceSnapshot fromCsv(BalanceSnapshotCsvRecord balanceSnapshotCsvRecord) {
        return new BalanceSnapshot(
                balanceSnapshotCsvRecord.snapshotId(),
                Instant.parse(balanceSnapshotCsvRecord.syncedAt()),
                balanceSnapshotCsvRecord.plaidItemId(),
                balanceSnapshotCsvRecord.accountId(),
                balanceSnapshotCsvRecord.accountName(),
                balanceSnapshotCsvRecord.accountMask(),
                balanceSnapshotCsvRecord.accountType(),
                balanceSnapshotCsvRecord.accountSubtype(),
                parseAmount(balanceSnapshotCsvRecord.currentBalance()),
                parseAmount(balanceSnapshotCsvRecord.availableBalance()),
                balanceSnapshotCsvRecord.isoCurrencyCode(),
                balanceSnapshotCsvRecord.unofficialCurrencyCode());
    }

    public BalanceSnapshotCsvRecord toCsv(BalanceSnapshot snapshot) {
        return new BalanceSnapshotCsvRecord(
                snapshot.snapshotId(),
                snapshot.syncedAt().toString(),
                snapshot.plaidItemId(),
                snapshot.accountId(),
                snapshot.accountName(),
                snapshot.accountMask(),
                snapshot.accountType(),
                snapshot.accountSubtype(),
                formatAmount(snapshot.currentBalance()),
                formatAmount(snapshot.availableBalance()),
                snapshot.isoCurrencyCode(),
                snapshot.unofficialCurrencyCode());
    }

    public BalanceSnapshotResponse toResponse(BalanceSnapshot snapshot) {
        return new BalanceSnapshotResponse(
                snapshot.snapshotId(),
                snapshot.syncedAt(),
                snapshot.accountId(),
                snapshot.accountName(),
                snapshot.accountMask(),
                snapshot.currentBalance(),
                snapshot.availableBalance(),
                snapshot.isoCurrencyCode());
    }

    private static BigDecimal parseAmount(String value) {
        return value == null || value.isBlank() ? null : new BigDecimal(value);
    }

    private static String formatAmount(BigDecimal value) {
        return value == null ? null : value.toPlainString();
    }

    private static BigDecimal amount(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static String value(Object value) {
        return value == null ? null : value.toString();
    }
}
