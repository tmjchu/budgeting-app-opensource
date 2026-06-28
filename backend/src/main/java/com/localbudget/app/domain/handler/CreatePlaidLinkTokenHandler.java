package com.localbudget.app.domain.handler;

import com.localbudget.app.domain.service.PlaidConnectionService;
import org.springframework.stereotype.Component;

@Component
public class CreatePlaidLinkTokenHandler {

    private final PlaidConnectionService plaidConnectionService;

    public CreatePlaidLinkTokenHandler(PlaidConnectionService plaidConnectionService) {
        this.plaidConnectionService = plaidConnectionService;
    }

    public String handle() {
        return plaidConnectionService.createLinkToken();
    }
}
