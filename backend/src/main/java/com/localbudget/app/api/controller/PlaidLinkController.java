package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.request.ExchangePublicTokenRequest;
import com.localbudget.app.api.model.response.AccountResponse;
import com.localbudget.app.api.model.response.LinkTokenResponse;
import com.localbudget.app.converter.AccountConverter;
import com.localbudget.app.domain.handler.CreatePlaidLinkTokenHandler;
import com.localbudget.app.domain.handler.ExchangePlaidPublicTokenHandler;
import com.localbudget.app.domain.model.command.ExchangePlaidPublicTokenCommand;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plaid")
public class PlaidLinkController {

    private final CreatePlaidLinkTokenHandler createPlaidLinkTokenHandler;
    private final ExchangePlaidPublicTokenHandler exchangePlaidPublicTokenHandler;
    private final AccountConverter accountConverter;

    public PlaidLinkController(
            CreatePlaidLinkTokenHandler createPlaidLinkTokenHandler,
            ExchangePlaidPublicTokenHandler exchangePlaidPublicTokenHandler,
            AccountConverter accountConverter) {
        this.createPlaidLinkTokenHandler = createPlaidLinkTokenHandler;
        this.exchangePlaidPublicTokenHandler = exchangePlaidPublicTokenHandler;
        this.accountConverter = accountConverter;
    }

    @PostMapping("/link-token")
    public LinkTokenResponse createLinkToken() {
        return new LinkTokenResponse(createPlaidLinkTokenHandler.handle());
    }

    @PostMapping("/exchange-public-token")
    public List<AccountResponse> exchangePublicToken(@Valid @RequestBody ExchangePublicTokenRequest request) {
        ExchangePlaidPublicTokenCommand command = new ExchangePlaidPublicTokenCommand(
                request.publicToken(),
                request.institutionName(),
                request.selectedAccounts().stream()
                        .map(accountConverter::toCommand)
                        .toList());
        return exchangePlaidPublicTokenHandler.handle(command).trackedAccounts().stream()
                .map(accountConverter::toResponse)
                .toList();
    }
}
