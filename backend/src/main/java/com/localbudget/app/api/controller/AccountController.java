package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.response.AccountResponse;
import com.localbudget.app.converter.AccountConverter;
import com.localbudget.app.domain.processor.GetAccountsProcessor;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final GetAccountsProcessor getAccountsProcessor;
    private final AccountConverter accountConverter;

    public AccountController(
            GetAccountsProcessor getAccountsProcessor, AccountConverter accountConverter) {
        this.getAccountsProcessor = getAccountsProcessor;
        this.accountConverter = accountConverter;
    }

    @GetMapping
    public List<AccountResponse> getAccounts() {
        return getAccountsProcessor.handle().stream().map(accountConverter::toResponse).toList();
    }
}
