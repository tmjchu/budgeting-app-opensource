package com.localbudget.app.domain.service;

import com.localbudget.app.converter.AccountConverter;
import com.localbudget.app.converter.PlaidItemConverter;
import com.localbudget.app.data.repository.PlaidItemCsvRepository;
import com.localbudget.app.domain.model.Account;
import com.localbudget.app.domain.model.PlaidItem;
import com.localbudget.app.domain.model.command.ExchangePlaidPublicTokenCommand;
import com.localbudget.app.domain.model.result.ExchangePlaidPublicTokenResult;
import com.localbudget.app.gateway.plaid.api.PlaidGateway;
import com.localbudget.app.gateway.plaid.model.PlaidExchangeResult;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class PlaidConnectionService {

    private final PlaidGateway plaidGateway;
    private final PlaidItemCsvRepository plaidItemRepository;
    private final AccountService accountService;
    private final PlaidItemConverter plaidItemConverter;
    private final AccountConverter accountConverter;
    private final Clock clock;

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

    public String createLinkToken() {
        return plaidGateway.createLinkToken();
    }

    public ExchangePlaidPublicTokenResult exchangePublicToken(ExchangePlaidPublicTokenCommand command) {
        PlaidExchangeResult exchangeResult = plaidGateway.exchangePublicToken(command.publicToken());
        PlaidItem plaidItem = new PlaidItem(
                exchangeResult.plaidItemId(),
                exchangeResult.accessToken(),
                command.institutionName(),
                Instant.now(clock));
        List<Account> accounts = command.selectedAccounts().stream()
                .map(selectedAccount -> accountConverter.fromSelectedAccount(plaidItem.plaidItemId(), selectedAccount))
                .toList();

        plaidItemRepository.upsert(plaidItemConverter.toCsv(plaidItem));
        accountService.saveAll(accounts);
        return new ExchangePlaidPublicTokenResult(plaidItem, accounts);
    }

    public List<PlaidItem> findConnectedItems() {
        return plaidItemRepository.findAll().stream()
                .map(plaidItemConverter::fromCsv)
                .toList();
    }
}
