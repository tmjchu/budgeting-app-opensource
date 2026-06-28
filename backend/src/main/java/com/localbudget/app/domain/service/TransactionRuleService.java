package com.localbudget.app.domain.service;

import com.localbudget.app.domain.model.Transaction;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TransactionRuleService {

    public List<Transaction> applyRules(List<Transaction> transactions) {
        return transactions.stream().map(this::markTransfersExcluded).toList();
    }

    private Transaction markTransfersExcluded(Transaction transaction) {
        String category = transaction.primaryCategory();
        boolean isTransfer = category != null && category.equalsIgnoreCase("TRANSFER");
        if (!isTransfer) {
            return transaction;
        }
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
                transaction.localCategory(),
                transaction.pending(),
                true,
                transaction.paymentChannel());
    }
}
