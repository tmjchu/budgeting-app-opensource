package com.localbudget.app.converter;

import com.localbudget.app.api.model.response.BalanceSnapshotResponse;
import com.localbudget.app.data.model.BalanceSnapshotCsvRecord;
import com.localbudget.app.domain.model.BalanceSnapshot;
import com.localbudget.app.gateway.plaid.model.PlaidBalance;
import java.math.BigDecimal;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class BalanceSnapshotConverter {

    public BalanceSnapshot fromGateway(PlaidBalance balance, Instant syncedAt) {
        String snapshotId = syncedAt + "_" + balance.accountId();
        return new BalanceSnapshot(
                snapshotId,
                syncedAt,
                balance.plaidItemId(),
                balance.accountId(),
                balance.accountName(),
                balance.accountMask(),
                balance.accountType(),
                balance.accountSubtype(),
                balance.currentBalance(),
                balance.availableBalance(),
                balance.isoCurrencyCode(),
                balance.unofficialCurrencyCode());
    }

    public BalanceSnapshot fromCsv(BalanceSnapshotCsvRecord record) {
        return new BalanceSnapshot(
                record.snapshotId(),
                Instant.parse(record.syncedAt()),
                record.plaidItemId(),
                record.accountId(),
                record.accountName(),
                record.accountMask(),
                record.accountType(),
                record.accountSubtype(),
                parseAmount(record.currentBalance()),
                parseAmount(record.availableBalance()),
                record.isoCurrencyCode(),
                record.unofficialCurrencyCode());
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
}
