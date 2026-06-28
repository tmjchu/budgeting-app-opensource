package com.localbudget.app.converter;

import com.localbudget.app.api.model.response.TransactionResponse;
import com.localbudget.app.data.model.TransactionCsvRecord;
import com.localbudget.app.domain.model.Transaction;
import com.localbudget.app.gateway.plaid.model.PlaidTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.springframework.stereotype.Component;

@Component
public class TransactionConverter {

    public Transaction fromGateway(PlaidTransaction transaction) {
        return new Transaction(
                transaction.transactionId(),
                transaction.plaidItemId(),
                transaction.accountId(),
                transaction.accountName(),
                transaction.date(),
                transaction.name(),
                transaction.merchantName(),
                transaction.amount(),
                transaction.primaryCategory(),
                transaction.detailedCategory(),
                null,
                transaction.pending(),
                false,
                transaction.paymentChannel());
    }

    public Transaction fromCsv(TransactionCsvRecord record) {
        return new Transaction(
                record.transactionId(),
                record.plaidItemId(),
                record.accountId(),
                record.accountName(),
                LocalDate.parse(record.date()),
                record.name(),
                record.merchantName(),
                parseAmount(record.amount()),
                record.primaryCategory(),
                record.detailedCategory(),
                record.localCategory(),
                Boolean.parseBoolean(record.pending()),
                Boolean.parseBoolean(record.excluded()),
                record.paymentChannel());
    }

    public TransactionCsvRecord toCsv(Transaction transaction) {
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

    public TransactionResponse toResponse(Transaction transaction) {
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
}
