package com.localbudget.app.domain.service;

import com.localbudget.app.converter.TransactionConverter;
import com.localbudget.app.domain.model.Account;
import com.localbudget.app.domain.model.PlaidItem;
import com.localbudget.app.domain.model.Transaction;
import com.localbudget.app.gateway.plaid.api.PlaidGateway;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TransactionFetchService {

    private final PlaidGateway plaidGateway;
    private final AccountService accountService;
    private final TransactionConverter transactionConverter;

    public TransactionFetchService(
            PlaidGateway plaidGateway,
            AccountService accountService,
            TransactionConverter transactionConverter) {
        this.plaidGateway = plaidGateway;
        this.accountService = accountService;
        this.transactionConverter = transactionConverter;
    }

    public List<Transaction> fetchTransactions(
            List<PlaidItem> plaidItems, LocalDate startDate, LocalDate endDate) {
        List<Transaction> transactions = new ArrayList<>();
        for (PlaidItem plaidItem : plaidItems) {
            List<Account> trackedAccounts =
                    accountService.findTrackedByPlaidItemId(plaidItem.plaidItemId());
            plaidGateway.fetchTransactions(plaidItem, trackedAccounts, startDate, endDate).stream()
                    .map(transactionConverter::fromGateway)
                    .forEach(transactions::add);
        }
        return transactions;
    }
}
