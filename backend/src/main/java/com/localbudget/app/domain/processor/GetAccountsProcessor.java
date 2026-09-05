package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.model.AccountView;
import com.localbudget.app.domain.service.AccountQueryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetAccountsProcessor {

    private final AccountQueryService accountService;

    public List<AccountView> process() {
        return accountService.findAll();
    }
}
