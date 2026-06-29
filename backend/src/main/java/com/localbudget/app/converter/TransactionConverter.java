package com.localbudget.app.converter;

import com.localbudget.app.api.model.response.TransactionResponse;
import com.localbudget.app.data.model.TransactionCsvRecord;
import com.localbudget.app.domain.model.AccountDO;
import com.localbudget.app.domain.model.PlaidItem;
import com.localbudget.app.domain.model.TransactionDO;
import com.plaid.client.model.PersonalFinanceCategory;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class TransactionConverter {

    public TransactionDO fromPlaid(
            PlaidItem plaidItem,
            List<AccountDO> trackedAccounts,
            com.plaid.client.model.Transaction transaction) {
        var accountById =
                trackedAccounts.stream()
                        .collect(Collectors.toMap(AccountDO::accountId, Function.identity()));
        AccountDO account = accountById.get(transaction.getAccountId());
        PersonalFinanceCategory category = transaction.getPersonalFinanceCategory();

        return new TransactionDO(
                transaction.getTransactionId(),
                plaidItem.plaidItemId(),
                transaction.getAccountId(),
                account == null ? null : account.name(),
                transaction.getDate(),
                transaction.getName(),
                transaction.getMerchantName(),
                amount(transaction.getAmount()),
                category == null ? null : category.getPrimary(),
                category == null ? null : category.getDetailed(),
                null,
                Boolean.TRUE.equals(transaction.getPending()),
                false,
                value(transaction.getPaymentChannel()));
    }

    public TransactionDO fromCsv(TransactionCsvRecord transactionCsvRecord) {
        return new TransactionDO(
                transactionCsvRecord.transactionId(),
                transactionCsvRecord.plaidItemId(),
                transactionCsvRecord.accountId(),
                transactionCsvRecord.accountName(),
                LocalDate.parse(transactionCsvRecord.date()),
                transactionCsvRecord.name(),
                transactionCsvRecord.merchantName(),
                parseAmount(transactionCsvRecord.amount()),
                transactionCsvRecord.primaryCategory(),
                transactionCsvRecord.detailedCategory(),
                transactionCsvRecord.localCategory(),
                Boolean.parseBoolean(transactionCsvRecord.pending()),
                Boolean.parseBoolean(transactionCsvRecord.excluded()),
                transactionCsvRecord.paymentChannel());
    }

    public TransactionCsvRecord toCsv(TransactionDO transaction) {
        return new TransactionCsvRecord(
                transaction.transactionId(),
                transaction.plaidItemId(),
                transaction.accountId(),
                transaction.accountName(),
                transaction.date().toString(),
                transaction.name(),
                transaction.merchantName(),
                transaction.amount().toPlainString(),
                transaction.primaryCategory(),
                transaction.detailedCategory(),
                transaction.localCategory(),
                String.valueOf(transaction.pending()),
                String.valueOf(transaction.excluded()),
                transaction.paymentChannel());
    }

    public TransactionResponse toResponse(TransactionDO transaction) {
        return new TransactionResponse(
                transaction.transactionId(),
                transaction.accountId(),
                transaction.accountName(),
                transaction.date(),
                transaction.name(),
                transaction.merchantName(),
                transaction.amount(),
                transaction.effectiveCategory(),
                transaction.pending(),
                transaction.excluded(),
                transaction.paymentChannel());
    }

    private static BigDecimal parseAmount(String value) {
        return value == null || value.isBlank() ? BigDecimal.ZERO : new BigDecimal(value);
    }

    private static BigDecimal amount(Double value) {
        return value == null ? null : BigDecimal.valueOf(value);
    }

    private static String value(Object value) {
        return value == null ? null : value.toString();
    }
}
