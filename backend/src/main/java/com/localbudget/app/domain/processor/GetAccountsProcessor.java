package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.model.AccountDO;
import com.localbudget.app.domain.service.AccountService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetAccountsProcessor {

    private final AccountService accountService;

    public List<AccountDO> process() {
        return accountService.findAll();
    }
}
