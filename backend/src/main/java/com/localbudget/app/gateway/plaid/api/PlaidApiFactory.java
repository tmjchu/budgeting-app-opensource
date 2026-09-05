package com.localbudget.app.gateway.plaid.api;

import static com.localbudget.app.config.BudgetAppProperties.Environment.PROD;

import com.localbudget.app.domain.model.PlaidCredentials;
import com.plaid.client.ApiClient;
import com.plaid.client.request.PlaidApi;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PlaidApiFactory {

    public PlaidApi create(PlaidCredentials credentials) {
        ApiClient apiClient = new ApiClient();
        if (credentials.environment() == PROD) {
            log.warn("Plaid production environment is enabled");
            apiClient.setPlaidAdapter(ApiClient.Production);
        } else {
            apiClient.setPlaidAdapter(ApiClient.Sandbox);
        }
        return apiClient.createService(PlaidApi.class);
    }
}
