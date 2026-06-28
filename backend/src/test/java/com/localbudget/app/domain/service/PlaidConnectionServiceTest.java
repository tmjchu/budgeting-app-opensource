package com.localbudget.app.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
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
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
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
    void createLinkTokenUsesCachedTokenBeforeTtlExpires() {
        when(plaidGateway.createLinkToken()).thenReturn("link-token-1");
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        PlaidConnectionService service = newService(clock);

        String firstToken = service.createLinkToken();
        clock.advance(Duration.ofMinutes(29));
        String secondToken = service.createLinkToken();

        assertThat(firstToken).isEqualTo("link-token-1");
        assertThat(secondToken).isEqualTo("link-token-1");
        verify(plaidGateway, times(1)).createLinkToken();
    }

    @Test
    void createLinkTokenRefreshesAfterTtlExpires() {
        when(plaidGateway.createLinkToken()).thenReturn("link-token-1", "link-token-2");
        MutableClock clock = new MutableClock(Instant.parse("2026-01-01T00:00:00Z"));
        PlaidConnectionService service = newService(clock);

        String firstToken = service.createLinkToken();
        clock.advance(Duration.ofMinutes(30));
        String secondToken = service.createLinkToken();

        assertThat(firstToken).isEqualTo("link-token-1");
        assertThat(secondToken).isEqualTo("link-token-2");
        verify(plaidGateway, times(2)).createLinkToken();
    }

    @Test
    void exchangePublicTokenStoresPlaidItemAndSelectedAccounts() {
        when(plaidGateway.exchangePublicToken("public-token"))
                .thenReturn(new PlaidExchangeResult("access-token", "item-1"));
        Clock clock = Clock.fixed(Instant.parse("2026-01-01T00:00:00Z"), ZoneOffset.UTC);
        PlaidConnectionService service = newService(clock);

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

    private PlaidConnectionService newService(Clock clock) {
        return new PlaidConnectionService(
                plaidGateway,
                plaidItemRepository,
                accountService,
                new PlaidItemConverter(),
                new AccountConverter(),
                clock);
    }

    private static class MutableClock extends Clock {

        private Instant instant;

        private MutableClock(Instant instant) {
            this.instant = instant;
        }

        private void advance(Duration duration) {
            instant = instant.plus(duration);
        }

        @Override
        public ZoneId getZone() {
            return ZoneOffset.UTC;
        }

        @Override
        public Clock withZone(ZoneId zone) {
            return this;
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
