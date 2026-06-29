package com.localbudget.app.domain.handler;

import com.localbudget.app.domain.model.AccountDO;
import com.localbudget.app.domain.service.AccountService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GetAccountsHandler {

    private final AccountService accountService;

    public GetAccountsHandler(AccountService accountService) {
        this.accountService = accountService;
    }

    public List<AccountDO> handle() {
        return accountService.findAll();
    }
}
