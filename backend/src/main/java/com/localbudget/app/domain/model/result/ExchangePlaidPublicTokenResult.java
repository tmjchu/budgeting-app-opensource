package com.localbudget.app.domain.model.result;

import com.localbudget.app.domain.model.Account;
import com.localbudget.app.domain.model.PlaidItem;
import java.util.List;

public record ExchangePlaidPublicTokenResult(
        PlaidItem plaidItem,
        List<Account> trackedAccounts
) {
}
