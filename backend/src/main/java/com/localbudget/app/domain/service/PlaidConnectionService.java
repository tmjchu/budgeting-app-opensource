package com.localbudget.app.domain.service;

import com.localbudget.app.converter.AccountConverter;
import com.localbudget.app.converter.PlaidItemConverter;
import com.localbudget.app.data.repository.PlaidItemCsvRepository;
import com.localbudget.app.domain.model.AccountDO;
import com.localbudget.app.domain.model.PlaidItem;
import com.localbudget.app.domain.model.command.ExchangePlaidPublicTokenCommand;
import com.localbudget.app.domain.model.result.ExchangePlaidPublicTokenResult;
import com.localbudget.app.gateway.plaid.api.PlaidGateway;
import com.localbudget.app.gateway.plaid.model.PlaidExchangeResult;
import com.plaid.client.model.LinkTokenCreateResponse;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PlaidConnectionService {

    private static final Duration LINK_TOKEN_TTL = Duration.ofMinutes(30);

    private final PlaidGateway plaidGateway;
    private final PlaidItemCsvRepository plaidItemRepository;
    private final AccountService accountService;
    private final PlaidItemConverter plaidItemConverter;
    private final AccountConverter accountConverter;
    private final Clock clock;
    private CachedLinkToken cachedLinkToken;

    public PlaidConnectionService(
            PlaidGateway plaidGateway,
            PlaidItemCsvRepository plaidItemRepository,
            AccountService accountService,
            PlaidItemConverter plaidItemConverter,
            AccountConverter accountConverter,
            Clock clock) {
        this.plaidGateway = plaidGateway;
        this.plaidItemRepository = plaidItemRepository;
        this.accountService = accountService;
        this.plaidItemConverter = plaidItemConverter;
        this.accountConverter = accountConverter;
        this.clock = clock;
    }

    public synchronized String createLinkToken() {
        Instant now = Instant.now(clock);
        if (cachedLinkToken != null && cachedLinkToken.expiresAt().isAfter(now)) {
            return cachedLinkToken.token();
        }

        LinkTokenCreateResponse response = plaidGateway.createLinkToken();
        String linkToken = response.getLinkToken();
        cachedLinkToken = new CachedLinkToken(linkToken, now.plus(LINK_TOKEN_TTL));
        return linkToken;
    }

    public ExchangePlaidPublicTokenResult exchangePublicToken(
            ExchangePlaidPublicTokenCommand command) {
        PlaidExchangeResult exchangeResult =
                plaidGateway.exchangePublicToken(command.publicToken());
        PlaidItem plaidItem =
                new PlaidItem(
                        exchangeResult.plaidItemId(),
                        exchangeResult.accessToken(),
                        command.institutionName(),
                        Instant.now(clock));
        List<AccountDO> accounts =
                command.selectedAccounts().stream()
                        .map(
                                selectedAccount ->
                                        accountConverter.fromSelectedAccount(
                                                plaidItem.plaidItemId(), selectedAccount))
                        .toList();

        plaidItemRepository.upsert(plaidItemConverter.toCsv(plaidItem));
        accountService.saveAll(accounts);
        return new ExchangePlaidPublicTokenResult(plaidItem, accounts);
    }

    public List<PlaidItem> findConnectedItems() {
        return plaidItemRepository.findAll().stream().map(plaidItemConverter::fromCsv).toList();
    }

    private record CachedLinkToken(String token, Instant expiresAt) {}
}
