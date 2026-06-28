package com.localbudget.app.gateway.plaid.api;

import com.localbudget.app.domain.model.Account;
import com.localbudget.app.domain.model.PlaidItem;
import com.localbudget.app.gateway.plaid.model.PlaidBalance;
import com.localbudget.app.gateway.plaid.model.PlaidExchangeResult;
import com.localbudget.app.gateway.plaid.model.PlaidTransaction;
import java.time.LocalDate;
import java.util.List;

public interface PlaidGateway {

    String createLinkToken();

    PlaidExchangeResult exchangePublicToken(String publicToken);

    List<PlaidTransaction> fetchTransactions(
            PlaidItem plaidItem,
            List<Account> trackedAccounts,
            LocalDate startDate,
            LocalDate endDate);

    List<PlaidBalance> fetchBalances(PlaidItem plaidItem, List<Account> trackedAccounts);
}
