package com.localbudget.app.gateway.plaid.api;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.domain.exception.PlaidCredentialsUnavailableException;
import com.localbudget.app.domain.service.PlaidCredentialsProvider;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

class PlaidSdkGatewayTest {

    @Test
    void defersCredentialRequirementUntilPlaidOperation() {
        PlaidCredentialsProvider credentialsProvider = mock(PlaidCredentialsProvider.class);
        PlaidApiFactory apiFactory = mock(PlaidApiFactory.class);
        when(credentialsProvider.requireCredentials())
                .thenThrow(new PlaidCredentialsUnavailableException());
        PlaidSdkGateway gateway =
                new PlaidSdkGateway(properties(), credentialsProvider, apiFactory);

        assertThatThrownBy(gateway::createLinkToken)
                .isInstanceOf(PlaidCredentialsUnavailableException.class);
        verifyNoInteractions(apiFactory);
    }

    private BudgetAppProperties properties() {
        return new BudgetAppProperties(
                Path.of("data"),
                new BudgetAppProperties.PlaidConfig(
                        BudgetAppProperties.Environment.SANDBOX,
                        "",
                        "",
                        "Open Budget",
                        List.of("transactions"),
                        List.of("US")));
    }
}
