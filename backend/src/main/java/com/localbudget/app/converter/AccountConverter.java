package com.localbudget.app.converter;

import com.localbudget.app.api.model.request.SelectedAccountRequest;
import com.localbudget.app.api.model.response.AccountResponse;
import com.localbudget.app.data.model.AccountCsvRecord;
import com.localbudget.app.domain.model.AccountDO;
import com.localbudget.app.domain.model.command.SelectedAccountCommand;
import org.springframework.stereotype.Component;

@Component
public class AccountConverter {

    public SelectedAccountCommand toCommand(SelectedAccountRequest request) {
        return new SelectedAccountCommand(
                request.accountId(),
                request.name(),
                request.mask(),
                request.type(),
                request.subtype());
    }

    public AccountDO fromSelectedAccount(
            String plaidItemId, SelectedAccountCommand selectedAccount) {
        return new AccountDO(
                selectedAccount.accountId(),
                plaidItemId,
                selectedAccount.name(),
                selectedAccount.mask(),
                selectedAccount.type(),
                selectedAccount.subtype(),
                true);
    }

    public AccountDO fromCsv(AccountCsvRecord accountCsvRecord) {
        return new AccountDO(
                accountCsvRecord.accountId(),
                accountCsvRecord.plaidItemId(),
                accountCsvRecord.name(),
                accountCsvRecord.mask(),
                accountCsvRecord.type(),
                accountCsvRecord.subtype(),
                Boolean.parseBoolean(accountCsvRecord.tracked()));
    }

    public AccountCsvRecord toCsv(AccountDO account) {
        return new AccountCsvRecord(
                account.accountId(),
                account.plaidItemId(),
                account.name(),
                account.mask(),
                account.type(),
                account.subtype(),
                String.valueOf(account.tracked()));
    }

    public AccountResponse toResponse(AccountDO account) {
        return new AccountResponse(
                account.accountId(),
                account.name(),
                account.mask(),
                account.type(),
                account.subtype(),
                account.tracked());
    }
}
