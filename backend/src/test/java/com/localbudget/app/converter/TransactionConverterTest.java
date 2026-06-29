package com.localbudget.app.converter;

import static org.assertj.core.api.Assertions.assertThat;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.data.model.TransactionCsvRecord;
import com.localbudget.app.domain.model.TransactionDO;
import com.plaid.client.model.PersonalFinanceCategory;
import com.plaid.client.model.Transaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class TransactionConverterTest {

    private final TransactionConverter converter = new TransactionConverter();

    @Test
    void fromPlaidMapsSdkTransactionToDomainObject() {
        Transaction plaidTransaction =
                new Transaction()
                        .transactionId("txn-1")
                        .accountId("acc-checking")
                        .date(LocalDate.parse("2026-06-28"))
                        .name("Coffee Shop")
                        .merchantName("Coffee Merchant")
                        .amount(12.34)
                        .personalFinanceCategory(
                                new PersonalFinanceCategory()
                                        .primary("FOOD_AND_DRINK")
                                        .detailed("FOOD_AND_DRINK_COFFEE"))
                        .pending(true)
                        .paymentChannel(Transaction.PaymentChannelEnum.IN_STORE);

        TransactionDO transaction =
                converter.fromPlaid(
                        TestFixtures.plaidItem(),
                        List.of(TestFixtures.checkingAccount()),
                        plaidTransaction);

        assertThat(transaction.transactionId()).isEqualTo("txn-1");
        assertThat(transaction.plaidItemId()).isEqualTo("item-1");
        assertThat(transaction.accountName()).isEqualTo("Main Checking");
        assertThat(transaction.amount()).isEqualByComparingTo("12.34");
        assertThat(transaction.primaryCategory()).isEqualTo("FOOD_AND_DRINK");
        assertThat(transaction.detailedCategory()).isEqualTo("FOOD_AND_DRINK_COFFEE");
        assertThat(transaction.localCategory()).isNull();
        assertThat(transaction.pending()).isTrue();
        assertThat(transaction.excluded()).isFalse();
        assertThat(transaction.paymentChannel()).isEqualTo("in store");
    }

    @Test
    void plaidMappedDomainObjectCanBeConvertedToCsvRecord() {
        Transaction plaidTransaction =
                new Transaction()
                        .transactionId("txn-2")
                        .accountId("acc-checking")
                        .date(LocalDate.parse("2026-06-29"))
                        .name("Paycheck")
                        .merchantName("Employer")
                        .amount(-2500.0)
                        .pending(false)
                        .paymentChannel(Transaction.PaymentChannelEnum.ONLINE);

        TransactionDO transaction =
                converter.fromPlaid(
                        TestFixtures.plaidItem(),
                        List.of(TestFixtures.checkingAccount()),
                        plaidTransaction);
        TransactionCsvRecord csvRecord = converter.toCsv(transaction);

        assertThat(csvRecord.transactionId()).isEqualTo("txn-2");
        assertThat(csvRecord.plaidItemId()).isEqualTo("item-1");
        assertThat(csvRecord.accountName()).isEqualTo("Main Checking");
        assertThat(new BigDecimal(csvRecord.amount())).isEqualByComparingTo("-2500.0");
        assertThat(csvRecord.pending()).isEqualTo("false");
        assertThat(csvRecord.excluded()).isEqualTo("false");
        assertThat(csvRecord.paymentChannel()).isEqualTo("online");
    }
}
