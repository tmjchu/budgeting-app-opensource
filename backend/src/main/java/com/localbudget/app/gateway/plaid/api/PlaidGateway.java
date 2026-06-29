package com.localbudget.app.gateway.plaid.api;

import com.localbudget.app.domain.model.AccountDO;
import com.localbudget.app.domain.model.PlaidItem;
import com.localbudget.app.gateway.plaid.model.PlaidExchangeResult;
import com.plaid.client.model.AccountBase;
import com.plaid.client.model.LinkTokenCreateResponse;
import com.plaid.client.model.Transaction;
import java.time.LocalDate;
import java.util.List;

public interface PlaidGateway {

    LinkTokenCreateResponse createLinkToken();

    PlaidExchangeResult exchangePublicToken(String publicToken);

    List<Transaction> fetchTransactions(
            PlaidItem plaidItem,
            List<AccountDO> trackedAccounts,
            LocalDate startDate,
            LocalDate endDate);

    List<AccountBase> fetchBalances(PlaidItem plaidItem, List<AccountDO> trackedAccounts);
}
