package com.localbudget.app.gateway.plaid.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.domain.model.Account;
import com.localbudget.app.domain.model.PlaidItem;
import com.localbudget.app.gateway.plaid.model.PlaidBalance;
import com.localbudget.app.gateway.plaid.model.PlaidExchangeResult;
import com.localbudget.app.gateway.plaid.model.PlaidTransaction;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class PlaidRestGateway implements PlaidGateway {

    private static final int PAGE_SIZE = 500;

    private final BudgetAppProperties properties;
    private final RestClient restClient;

    public PlaidRestGateway(BudgetAppProperties properties, RestClient.Builder restClientBuilder) {
        this.properties = properties;
        this.restClient = restClientBuilder.baseUrl(properties.plaid().baseUrl()).build();
    }

    @Override
    public String createLinkToken() {
        Map<String, Object> body = baseCredentials();
        body.put("client_name", properties.plaid().clientName());
        body.put("language", "en");
        body.put("country_codes", properties.plaid().countryCodes());
        body.put("products", properties.plaid().products());
        body.put("user", Map.of("client_user_id", "local-" + UUID.randomUUID()));

        JsonNode response = post("/link/token/create", body);
        return text(response, "link_token");
    }

    @Override
    public PlaidExchangeResult exchangePublicToken(String publicToken) {
        Map<String, Object> body = baseCredentials();
        body.put("public_token", publicToken);

        JsonNode response = post("/item/public_token/exchange", body);
        return new PlaidExchangeResult(text(response, "access_token"), text(response, "item_id"));
    }

    @Override
    public List<PlaidTransaction> fetchTransactions(
            PlaidItem plaidItem,
            List<Account> trackedAccounts,
            LocalDate startDate,
            LocalDate endDate) {
        Map<String, Account> accountById =
                trackedAccounts.stream()
                        .collect(Collectors.toMap(Account::accountId, Function.identity()));
        List<String> accountIds = trackedAccounts.stream().map(Account::accountId).toList();
        List<PlaidTransaction> transactions = new ArrayList<>();
        int total = Integer.MAX_VALUE;
        int offset = 0;

        while (offset < total) {
            Map<String, Object> body = baseCredentials();
            body.put("access_token", plaidItem.accessToken());
            body.put("start_date", startDate.toString());
            body.put("end_date", endDate.toString());
            body.put(
                    "options",
                    Map.of(
                            "account_ids", accountIds,
                            "count", PAGE_SIZE,
                            "offset", offset));

            JsonNode response = post("/transactions/get", body);
            total = response.path("total_transactions").asInt(0);
            for (JsonNode node : response.path("transactions")) {
                String accountId = text(node, "account_id");
                Account account = accountById.get(accountId);
                transactions.add(
                        new PlaidTransaction(
                                plaidItem.plaidItemId(),
                                text(node, "transaction_id"),
                                accountId,
                                account == null ? null : account.name(),
                                LocalDate.parse(text(node, "date")),
                                text(node, "name"),
                                text(node, "merchant_name"),
                                amount(node.path("amount")),
                                category(node, "primary"),
                                category(node, "detailed"),
                                node.path("pending").asBoolean(false),
                                text(node, "payment_channel")));
            }
            offset += PAGE_SIZE;
        }

        return transactions;
    }

    @Override
    public List<PlaidBalance> fetchBalances(PlaidItem plaidItem, List<Account> trackedAccounts) {
        Map<String, Account> accountById =
                trackedAccounts.stream()
                        .collect(Collectors.toMap(Account::accountId, Function.identity()));
        List<String> accountIds = trackedAccounts.stream().map(Account::accountId).toList();
        Map<String, Object> body = baseCredentials();
        body.put("access_token", plaidItem.accessToken());
        body.put("options", Map.of("account_ids", accountIds));

        JsonNode response = post("/accounts/balance/get", body);
        List<PlaidBalance> balances = new ArrayList<>();
        for (JsonNode node : response.path("accounts")) {
            String accountId = text(node, "account_id");
            Account trackedAccount = accountById.get(accountId);
            JsonNode balance = node.path("balances");
            balances.add(
                    new PlaidBalance(
                            plaidItem.plaidItemId(),
                            accountId,
                            trackedAccount == null ? text(node, "name") : trackedAccount.name(),
                            trackedAccount == null ? text(node, "mask") : trackedAccount.mask(),
                            trackedAccount == null ? text(node, "type") : trackedAccount.type(),
                            trackedAccount == null
                                    ? text(node, "subtype")
                                    : trackedAccount.subtype(),
                            amount(balance.path("current")),
                            amount(balance.path("available")),
                            text(balance, "iso_currency_code"),
                            text(balance, "unofficial_currency_code")));
        }
        return balances;
    }

    private JsonNode post(String path, Map<String, Object> body) {
        validateCredentials();
        return restClient.post().uri(path).body(body).retrieve().body(JsonNode.class);
    }

    private Map<String, Object> baseCredentials() {
        Map<String, Object> credentials = new LinkedHashMap<>();
        credentials.put("client_id", properties.plaid().clientId());
        credentials.put("secret", properties.plaid().secret());
        return credentials;
    }

    private void validateCredentials() {
        if (isBlank(properties.plaid().clientId()) || isBlank(properties.plaid().secret())) {
            throw new IllegalStateException("PLAID_CLIENT_ID and PLAID_SECRET must be configured.");
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private static String category(JsonNode transaction, String key) {
        return text(transaction.path("personal_finance_category"), key);
    }

    private static BigDecimal amount(JsonNode node) {
        if (node == null || node.isMissingNode() || node.isNull()) {
            return null;
        }
        return new BigDecimal(node.asText());
    }

    private static String text(JsonNode node, String key) {
        JsonNode value = node.path(key);
        return value.isMissingNode() || value.isNull() ? null : value.asText();
    }
}
