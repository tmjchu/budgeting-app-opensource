package com.localbudget.app.domain.handler;

import com.localbudget.app.domain.model.command.ExchangePlaidPublicTokenCommand;
import com.localbudget.app.domain.model.result.ExchangePlaidPublicTokenResult;
import com.localbudget.app.domain.service.PlaidConnectionService;
import org.springframework.stereotype.Component;

@Component
public class ExchangePlaidPublicTokenHandler {

    private final PlaidConnectionService plaidConnectionService;

    public ExchangePlaidPublicTokenHandler(PlaidConnectionService plaidConnectionService) {
        this.plaidConnectionService = plaidConnectionService;
    }

    public ExchangePlaidPublicTokenResult handle(ExchangePlaidPublicTokenCommand command) {
        return plaidConnectionService.exchangePublicToken(command);
    }
}
