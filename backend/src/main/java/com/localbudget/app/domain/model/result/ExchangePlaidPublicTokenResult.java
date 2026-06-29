package com.localbudget.app.domain.model.result;

import com.localbudget.app.domain.model.AccountDO;
import com.localbudget.app.domain.model.PlaidItem;
import java.util.List;

public record ExchangePlaidPublicTokenResult(
        PlaidItem plaidItem, List<AccountDO> trackedAccounts) {}
