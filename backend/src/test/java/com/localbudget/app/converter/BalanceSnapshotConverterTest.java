package com.localbudget.app.converter;

import static org.assertj.core.api.Assertions.assertThat;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.data.model.BalanceSnapshotCsvRecord;
import com.localbudget.app.domain.model.BalanceSnapshot;
import com.plaid.client.model.AccountBalance;
import com.plaid.client.model.AccountBase;
import com.plaid.client.model.AccountSubtype;
import com.plaid.client.model.AccountType;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class BalanceSnapshotConverterTest {

    private final BalanceSnapshotConverter converter = new BalanceSnapshotConverter();

    @Test
    void fromPlaidMapsSdkAccountToDomainObjectUsingTrackedAccountMetadata() {
        Instant syncedAt = Instant.parse("2026-06-28T12:00:00Z");
        AccountBase plaidAccount =
                new AccountBase()
                        .accountId("acc-checking")
                        .name("Plaid Checking")
                        .mask("9999")
                        .type(AccountType.DEPOSITORY)
                        .subtype(AccountSubtype.CHECKING)
                        .balances(
                                new AccountBalance()
                                        .current(200.00)
                                        .available(180.00)
                                        .isoCurrencyCode("USD"));

        BalanceSnapshot snapshot =
                converter.fromPlaid(
                        TestFixtures.plaidItem(),
                        List.of(TestFixtures.checkingAccount()),
                        plaidAccount,
                        syncedAt);

        assertThat(snapshot.snapshotId()).isEqualTo("2026-06-28T12:00:00Z_acc-checking");
        assertThat(snapshot.plaidItemId()).isEqualTo("item-1");
        assertThat(snapshot.accountName()).isEqualTo("Main Checking");
        assertThat(snapshot.accountMask()).isEqualTo("1234");
        assertThat(snapshot.accountType()).isEqualTo("depository");
        assertThat(snapshot.accountSubtype()).isEqualTo("checking");
        assertThat(snapshot.currentBalance()).isEqualByComparingTo("200.0");
        assertThat(snapshot.availableBalance()).isEqualByComparingTo("180.0");
        assertThat(snapshot.isoCurrencyCode()).isEqualTo("USD");
    }

    @Test
    void plaidMappedDomainObjectCanBeConvertedToCsvRecord() {
        Instant syncedAt = Instant.parse("2026-06-28T12:00:00Z");
        AccountBase plaidAccount =
                new AccountBase()
                        .accountId("acc-savings")
                        .name("Savings")
                        .mask("4321")
                        .type(AccountType.DEPOSITORY)
                        .subtype(AccountSubtype.SAVINGS)
                        .balances(
                                new AccountBalance()
                                        .current(500.25)
                                        .available(490.25)
                                        .isoCurrencyCode("USD"));

        BalanceSnapshot snapshot =
                converter.fromPlaid(TestFixtures.plaidItem(), List.of(), plaidAccount, syncedAt);
        BalanceSnapshotCsvRecord csvRecord = converter.toCsv(snapshot);

        assertThat(csvRecord.snapshotId()).isEqualTo("2026-06-28T12:00:00Z_acc-savings");
        assertThat(csvRecord.plaidItemId()).isEqualTo("item-1");
        assertThat(csvRecord.accountName()).isEqualTo("Savings");
        assertThat(csvRecord.accountMask()).isEqualTo("4321");
        assertThat(csvRecord.accountType()).isEqualTo("depository");
        assertThat(csvRecord.accountSubtype()).isEqualTo("savings");
        assertThat(new BigDecimal(csvRecord.currentBalance())).isEqualByComparingTo("500.25");
        assertThat(new BigDecimal(csvRecord.availableBalance())).isEqualByComparingTo("490.25");
        assertThat(csvRecord.isoCurrencyCode()).isEqualTo("USD");
    }
}
