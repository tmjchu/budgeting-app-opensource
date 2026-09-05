package com.localbudget.app.gateway.plaid.api;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.domain.exception.CredentialOperationException;
import com.localbudget.app.domain.model.PlaidCredentials;
import com.localbudget.app.domain.service.PlaidCredentialValidator;
import com.plaid.client.model.CountryCode;
import com.plaid.client.model.LinkTokenCreateRequest;
import com.plaid.client.model.LinkTokenCreateRequestUser;
import com.plaid.client.model.Products;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;
import retrofit2.Response;

@Component
public class PlaidSdkCredentialValidator implements PlaidCredentialValidator {

    private final BudgetAppProperties.PlaidConfig plaidConfig;
    private final PlaidApiFactory apiFactory;

    public PlaidSdkCredentialValidator(BudgetAppProperties properties, PlaidApiFactory apiFactory) {
        this.plaidConfig = properties.plaid();
        this.apiFactory = apiFactory;
    }

    @Override
    public void validate(PlaidCredentials credentials) {
        LinkTokenCreateRequest request =
                new LinkTokenCreateRequest()
                        .clientId(credentials.clientId())
                        .secret(credentials.secret())
                        .clientName(plaidConfig.clientName())
                        .language("en")
                        .countryCodes(toCountryCodes(plaidConfig.countryCodes()))
                        .products(toProducts(plaidConfig.products()))
                        .user(
                                new LinkTokenCreateRequestUser()
                                        .clientUserId("setup-" + UUID.randomUUID()));
        try {
            Response<?> response =
                    apiFactory.create(credentials).linkTokenCreate(request).execute();
            if (!response.isSuccessful() || response.body() == null) {
                throw new CredentialOperationException("Plaid rejected the supplied credentials.");
            }
        } catch (IOException exception) {
            throw new CredentialOperationException("Unable to validate Plaid credentials.");
        }
    }

    private List<Products> toProducts(List<String> products) {
        return products.stream().map(Products::fromValue).toList();
    }

    private List<CountryCode> toCountryCodes(List<String> countryCodes) {
        return countryCodes.stream().map(CountryCode::fromValue).toList();
    }
}
