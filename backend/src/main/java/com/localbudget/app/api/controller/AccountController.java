package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.response.AccountResponse;
import com.localbudget.app.converter.AccountConverter;
import com.localbudget.app.domain.processor.GetAccountsProcessor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final GetAccountsProcessor getAccountsProcessor;
    private final AccountConverter accountConverter;

    @GetMapping
    public List<AccountResponse> getAccounts() {
        log.info("Accounts API Invoked");
        List<AccountResponse> response =
                getAccountsProcessor.process().stream().map(accountConverter::toResponse).toList();
        log.info("Accounts API Completed");
        return response;
    }
}
