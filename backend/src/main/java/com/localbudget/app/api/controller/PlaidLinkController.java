package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.request.ExchangePublicTokenRequest;
import com.localbudget.app.api.model.response.AccountResponse;
import com.localbudget.app.api.model.response.LinkTokenResponse;
import com.localbudget.app.converter.AccountConverter;
import com.localbudget.app.domain.model.command.ExchangePlaidPublicTokenCommand;
import com.localbudget.app.domain.processor.CreatePlaidLinkTokenProcessor;
import com.localbudget.app.domain.processor.ExchangePlaidPublicTokenProcessor;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/plaid")
@RequiredArgsConstructor
public class PlaidLinkController {

    private final CreatePlaidLinkTokenProcessor createPlaidLinkTokenProcessor;
    private final ExchangePlaidPublicTokenProcessor exchangePlaidPublicTokenProcessor;
    private final AccountConverter accountConverter;

    @PostMapping("/link-token")
    public LinkTokenResponse createLinkToken() {
        return new LinkTokenResponse(createPlaidLinkTokenProcessor.handle());
    }

    @PostMapping("/exchange-public-token")
    public List<AccountResponse> exchangePublicToken(
            @Valid @RequestBody ExchangePublicTokenRequest request) {
        ExchangePlaidPublicTokenCommand command =
                new ExchangePlaidPublicTokenCommand(
                        request.publicToken(),
                        request.institutionName(),
                        request.selectedAccounts().stream()
                                .map(accountConverter::toCommand)
                                .toList());
        return exchangePlaidPublicTokenProcessor.handle(command).trackedAccounts().stream()
                .map(accountConverter::toResponse)
                .toList();
    }
}
