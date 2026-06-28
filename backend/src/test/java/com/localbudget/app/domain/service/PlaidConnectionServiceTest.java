package com.localbudget.app.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.localbudget.app.converter.AccountConverter;
import com.localbudget.app.converter.PlaidItemConverter;
import com.localbudget.app.data.model.PlaidItemCsvRecord;
import com.localbudget.app.data.repository.PlaidItemCsvRepository;
import com.localbudget.app.domain.model.command.ExchangePlaidPublicTokenCommand;
import com.localbudget.app.domain.model.command.SelectedAccountCommand;
import com.localbudget.app.domain.model.result.ExchangePlaidPublicTokenResult;
import com.localbudget.app.gateway.plaid.api.PlaidGateway;
import com.localbudget.app.gateway.plaid.model.PlaidExchangeResult;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PlaidConnectionServiceTest {

    @Mock private PlaidGateway plaidGateway;
    @Mock private PlaidItemCsvRepository plaidItemRepository;
    @Mock private AccountService accountService;

    @Test
    void exchangePublicTokenStoresPlaidItemAndSelectedAccounts() {
        when(plaidGateway.exchangePublicToken("public-token"))
                .thenReturn(new PlaidExchangeResult("access-token", "item-1"));
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        PlaidConnectionService service =
                new PlaidConnectionService(
                        plaidGateway,
                        plaidItemRepository,
                        accountService,
                        new PlaidItemConverter(),
                        new AccountConverter(),
                        clock);

        ExchangePlaidPublicTokenResult result =
                service.exchangePublicToken(
                        new ExchangePlaidPublicTokenCommand(
                                "public-token",
                                "Test Bank",
                                List.of(
                                        new SelectedAccountCommand(
                                                "acc-1",
                                                "Checking",
                                                "1234",
                                                "depository",
                                                "checking"))));

        assertThat(result.plaidItem().accessToken()).isEqualTo("access-token");
        assertThat(result.trackedAccounts()).hasSize(1);
        ArgumentCaptor<PlaidItemCsvRecord> itemCaptor =
                ArgumentCaptor.forClass(PlaidItemCsvRecord.class);
        verify(plaidItemRepository).upsert(itemCaptor.capture());
        assertThat(itemCaptor.getValue().createdAt()).isEqualTo("2026-01-01T00:00:00Z");
        verify(accountService).saveAll(result.trackedAccounts());
    }
}
