package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.response.AccountResponse;
import com.localbudget.app.converter.AccountConverter;
import com.localbudget.app.domain.handler.GetAccountsHandler;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final GetAccountsHandler getAccountsHandler;
    private final AccountConverter accountConverter;

    public AccountController(GetAccountsHandler getAccountsHandler, AccountConverter accountConverter) {
        this.getAccountsHandler = getAccountsHandler;
        this.accountConverter = accountConverter;
    }

    @GetMapping
    public List<AccountResponse> getAccounts() {
        return getAccountsHandler.handle().stream()
                .map(accountConverter::toResponse)
                .toList();
    }
}
