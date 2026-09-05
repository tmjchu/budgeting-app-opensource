package com.localbudget.app.domain.service;

import com.localbudget.app.domain.model.AccountDO;
import com.localbudget.app.domain.model.AccountView;
import com.localbudget.app.domain.model.PlaidItem;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountQueryService {
    private final AccountService accountService;
    private final PlaidConnectionService plaidConnectionService;

    public List<AccountView> findAll() {
        return enrich(accountService.findAll(), plaidConnectionService.findConnectedItems());
    }

    public List<AccountView> enrich(List<AccountDO> accounts, List<PlaidItem> items) {
        Map<String, PlaidItem> byId =
                items.stream()
                        .collect(Collectors.toMap(PlaidItem::plaidItemId, Function.identity()));
        return accounts.stream()
                .map(
                        account -> {
                            PlaidItem item = byId.get(account.plaidItemId());
                            return new AccountView(
                                    account,
                                    item == null ? null : item.institutionId(),
                                    item == null ? null : item.institutionName());
                        })
                .toList();
    }
}
