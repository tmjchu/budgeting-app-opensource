package com.localbudget.app.domain.service;

import com.localbudget.app.domain.model.TransactionDO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TransactionRuleService {

    public List<TransactionDO> applyRules(List<TransactionDO> transactions) {
        return transactions.stream().map(this::markTransfersExcluded).toList();
    }

    private TransactionDO markTransfersExcluded(TransactionDO transaction) {
        String category = transaction.primaryCategory();
        boolean isTransfer = category != null && category.equalsIgnoreCase("TRANSFER");
        if (!isTransfer) {
            return transaction;
        }
        return new TransactionDO(
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
