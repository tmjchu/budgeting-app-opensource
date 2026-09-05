package com.localbudget.app.gateway.plaid.api;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.domain.model.AccountDO;
import com.localbudget.app.domain.model.PlaidCredentials;
import com.localbudget.app.domain.model.PlaidItem;
import com.localbudget.app.domain.service.PlaidCredentialsProvider;
import com.localbudget.app.gateway.plaid.model.PlaidExchangeResult;
import com.plaid.client.model.AccountBase;
import com.plaid.client.model.AccountsBalanceGetRequest;
import com.plaid.client.model.AccountsBalanceGetRequestOptions;
import com.plaid.client.model.AccountsGetResponse;
import com.plaid.client.model.CountryCode;
import com.plaid.client.model.ItemPublicTokenExchangeRequest;
import com.plaid.client.model.ItemPublicTokenExchangeResponse;
import com.plaid.client.model.LinkTokenCreateRequest;
import com.plaid.client.model.LinkTokenCreateRequestUser;
import com.plaid.client.model.LinkTokenCreateResponse;
import com.plaid.client.model.Products;
import com.plaid.client.model.Transaction;
import com.plaid.client.model.TransactionsGetRequest;
import com.plaid.client.model.TransactionsGetRequestOptions;
import com.plaid.client.model.TransactionsGetResponse;
import com.plaid.client.request.PlaidApi;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import retrofit2.Response;

@Component
public class PlaidSdkGateway implements PlaidGateway {

    private static final int PAGE_SIZE = 500;
    private static final String ENGLISH = "en";
    private static final String RANDOM_CLIENT_ID_PREFIX = "local-";

    private final BudgetAppProperties.PlaidConfig plaidConfig;
    private final PlaidCredentialsProvider credentialsProvider;
    private final PlaidApiFactory apiFactory;

    public PlaidSdkGateway(
            BudgetAppProperties properties,
            PlaidCredentialsProvider credentialsProvider,
            PlaidApiFactory apiFactory) {
        this.plaidConfig = properties.plaid();
        this.credentialsProvider = credentialsProvider;
        this.apiFactory = apiFactory;
    }

    @Override
    public LinkTokenCreateResponse createLinkToken() {
        PlaidCredentials credentials = credentialsProvider.requireCredentials();
        PlaidApi plaidApi = apiFactory.create(credentials);

        LinkTokenCreateRequest request =
                new LinkTokenCreateRequest()
                        .clientId(credentials.clientId())
                        .secret(credentials.secret())
                        .clientName(plaidConfig.clientName())
                        .language(ENGLISH)
                        .countryCodes(toCountryCodes(plaidConfig.countryCodes()))
                        .products(toProducts(plaidConfig.products()))
                        .user(
                                new LinkTokenCreateRequestUser()
                                        .clientUserId(RANDOM_CLIENT_ID_PREFIX + UUID.randomUUID()));

        return execute(plaidApi.linkTokenCreate(request));
    }

    @Override
    public PlaidExchangeResult exchangePublicToken(String publicToken) {
        PlaidCredentials credentials = credentialsProvider.requireCredentials();
        PlaidApi plaidApi = apiFactory.create(credentials);
        ItemPublicTokenExchangeRequest request =
                new ItemPublicTokenExchangeRequest()
                        .clientId(credentials.clientId())
                        .secret(credentials.secret())
                        .publicToken(publicToken);

        ItemPublicTokenExchangeResponse response =
                execute(plaidApi.itemPublicTokenExchange(request));
        return new PlaidExchangeResult(response.getAccessToken(), response.getItemId());
    }

    @Override
    public List<Transaction> fetchTransactions(
            PlaidItem plaidItem,
            List<AccountDO> trackedAccounts,
            LocalDate startDate,
            LocalDate endDate) {
        PlaidCredentials credentials = credentialsProvider.requireCredentials();
        PlaidApi plaidApi = apiFactory.create(credentials);
        List<String> accountIds = trackedAccounts.stream().map(AccountDO::accountId).toList();
        List<Transaction> transactions = new ArrayList<>();
        int total = Integer.MAX_VALUE;
        int offset = 0;

        while (offset < total) {
            TransactionsGetRequest request =
                    new TransactionsGetRequest()
                            .clientId(credentials.clientId())
                            .secret(credentials.secret())
                            .accessToken(plaidItem.accessToken())
                            .startDate(startDate)
                            .endDate(endDate)
                            .options(
                                    new TransactionsGetRequestOptions()
                                            .accountIds(accountIds)
                                            .count(PAGE_SIZE)
                                            .offset(offset)
                                            .includePersonalFinanceCategory(true));

            TransactionsGetResponse response = execute(plaidApi.transactionsGet(request));
            total = response.getTotalTransactions() == null ? 0 : response.getTotalTransactions();
            transactions.addAll(response.getTransactions());
            offset += PAGE_SIZE;
        }

        return transactions;
    }

    @Override
    public List<AccountBase> fetchBalances(PlaidItem plaidItem, List<AccountDO> trackedAccounts) {
        PlaidCredentials credentials = credentialsProvider.requireCredentials();
        PlaidApi plaidApi = apiFactory.create(credentials);
        List<String> accountIds = trackedAccounts.stream().map(AccountDO::accountId).toList();
        AccountsBalanceGetRequest request =
                new AccountsBalanceGetRequest()
                        .clientId(credentials.clientId())
                        .secret(credentials.secret())
                        .accessToken(plaidItem.accessToken())
                        .options(new AccountsBalanceGetRequestOptions().accountIds(accountIds));

        AccountsGetResponse response = execute(plaidApi.accountsBalanceGet(request));

        return response.getAccounts();
    }

    private <T> T execute(Call<T> call) {
        try {
            Response<T> response = call.execute();
            if (response.isSuccessful() && response.body() != null) {
                return response.body();
            }

            String errorBody = response.errorBody() == null ? "" : response.errorBody().string();
            throw new IllegalStateException("Plaid request failed: " + errorBody);
        } catch (IOException exception) {
            throw new IllegalStateException("Plaid request failed.", exception);
        }
    }

    private static List<Products> toProducts(List<String> products) {
        return products.stream().map(Products::fromValue).toList();
    }

    private static List<CountryCode> toCountryCodes(List<String> countryCodes) {
        return countryCodes.stream().map(CountryCode::fromValue).toList();
    }
}
