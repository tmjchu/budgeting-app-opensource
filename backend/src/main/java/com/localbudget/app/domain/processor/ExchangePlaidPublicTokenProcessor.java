package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.model.command.ExchangePlaidPublicTokenCommand;
import com.localbudget.app.domain.model.result.ExchangePlaidPublicTokenResult;
import com.localbudget.app.domain.service.PlaidConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExchangePlaidPublicTokenProcessor {

    private final PlaidConnectionService plaidConnectionService;

    public ExchangePlaidPublicTokenResult process(ExchangePlaidPublicTokenCommand command) {
        return plaidConnectionService.exchangePublicToken(command);
    }
}
