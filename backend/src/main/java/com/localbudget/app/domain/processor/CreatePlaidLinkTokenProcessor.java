package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.service.PlaidConnectionService;
import org.springframework.stereotype.Component;

@Component
public class CreatePlaidLinkTokenProcessor {

    private final PlaidConnectionService plaidConnectionService;

    public CreatePlaidLinkTokenProcessor(PlaidConnectionService plaidConnectionService) {
        this.plaidConnectionService = plaidConnectionService;
    }

    public String handle() {
        return plaidConnectionService.createLinkToken();
    }
}
