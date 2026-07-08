package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.service.PlaidConnectionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CreatePlaidLinkTokenProcessor {

    private final PlaidConnectionService plaidConnectionService;

    public String process() {
        return plaidConnectionService.createLinkToken();
    }
}
