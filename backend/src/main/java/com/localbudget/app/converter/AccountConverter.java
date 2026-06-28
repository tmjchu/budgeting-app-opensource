package com.localbudget.app.converter;

import com.localbudget.app.api.model.request.SelectedAccountRequest;
import com.localbudget.app.api.model.response.AccountResponse;
import com.localbudget.app.data.model.AccountCsvRecord;
import com.localbudget.app.domain.model.Account;
import com.localbudget.app.domain.model.command.SelectedAccountCommand;
import com.localbudget.app.gateway.plaid.model.PlaidLinkedAccount;
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

    public Account fromSelectedAccount(String plaidItemId, SelectedAccountCommand selectedAccount) {
        return new Account(
                selectedAccount.accountId(),
                plaidItemId,
                selectedAccount.name(),
                selectedAccount.mask(),
                selectedAccount.type(),
                selectedAccount.subtype(),
                true);
    }

    public Account fromGateway(String plaidItemId, PlaidLinkedAccount account, boolean tracked) {
        return new Account(
                account.accountId(),
                plaidItemId,
                account.name(),
                account.mask(),
                account.type(),
                account.subtype(),
                tracked);
    }

    public Account fromCsv(AccountCsvRecord record) {
        return new Account(
                record.accountId(),
                record.plaidItemId(),
                record.name(),
                record.mask(),
                record.type(),
                record.subtype(),
                Boolean.parseBoolean(record.tracked()));
    }

    public AccountCsvRecord toCsv(Account account) {
        return new AccountCsvRecord(
                account.accountId(),
                account.plaidItemId(),
                account.name(),
                account.mask(),
                account.type(),
                account.subtype(),
                String.valueOf(account.tracked()));
    }

    public AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.accountId(),
                account.name(),
                account.mask(),
                account.type(),
                account.subtype(),
                account.tracked());
    }
}
