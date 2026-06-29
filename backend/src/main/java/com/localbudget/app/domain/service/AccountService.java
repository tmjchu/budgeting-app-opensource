package com.localbudget.app.domain.service;

import com.localbudget.app.converter.AccountConverter;
import com.localbudget.app.data.repository.AccountCsvRepository;
import com.localbudget.app.domain.model.AccountDO;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class AccountService {

    private final AccountCsvRepository accountRepository;
    private final AccountConverter accountConverter;

    public AccountService(
            AccountCsvRepository accountRepository, AccountConverter accountConverter) {
        this.accountRepository = accountRepository;
        this.accountConverter = accountConverter;
    }

    public List<AccountDO> findAll() {
        return accountRepository.findAll().stream().map(accountConverter::fromCsv).toList();
    }

    public List<AccountDO> findTrackedByPlaidItemId(String plaidItemId) {
        return accountRepository.findTracked().stream()
                .map(accountConverter::fromCsv)
                .filter(account -> plaidItemId.equals(account.plaidItemId()))
                .toList();
    }

    public void saveAll(List<AccountDO> accounts) {
        accountRepository.upsertAll(accounts.stream().map(accountConverter::toCsv).toList());
    }
}
