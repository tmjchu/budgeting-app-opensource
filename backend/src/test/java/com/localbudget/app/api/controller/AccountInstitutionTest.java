package com.localbudget.app.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

import com.localbudget.app.api.model.request.ExchangePublicTokenRequest;
import com.localbudget.app.api.model.request.SelectedAccountRequest;
import com.localbudget.app.converter.AccountConverter;
import com.localbudget.app.domain.model.AccountDO;
import com.localbudget.app.domain.model.PlaidItem;
import com.localbudget.app.domain.model.command.ExchangePlaidPublicTokenCommand;
import com.localbudget.app.domain.model.result.ExchangePlaidPublicTokenResult;
import com.localbudget.app.domain.processor.CreatePlaidLinkTokenProcessor;
import com.localbudget.app.domain.processor.ExchangePlaidPublicTokenProcessor;
import com.localbudget.app.domain.processor.GetAccountsProcessor;
import com.localbudget.app.domain.service.AccountQueryService;
import com.localbudget.app.domain.service.AccountService;
import com.localbudget.app.domain.service.PlaidConnectionService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import tools.jackson.databind.ObjectMapper;

class AccountInstitutionTest {
    private final AccountService accounts = mock(AccountService.class);
    private final PlaidConnectionService connections = mock(PlaidConnectionService.class);
    private final AccountQueryService query = new AccountQueryService(accounts, connections);
    private final AccountConverter converter = new AccountConverter();

    @Test
    void joinsEachAccountOnceAndToleratesMissingInstitutionsWithoutLeakingSecrets() {
        var first = account("a", "item-a");
        var second = account("b", "item-b");
        var orphan = account("c", "missing");
        when(accounts.findAll()).thenReturn(List.of(first, second, orphan));
        when(connections.findConnectedItems())
                .thenReturn(
                        List.of(
                                item("item-b", "Citi", "test-citi"),
                                item("item-a", "Chase", null)));
        var response =
                new AccountController(new GetAccountsProcessor(query), converter).getAccounts();
        assertThat(response)
                .extracting(r -> r.institutionName())
                .containsExactly("Chase", "Citi", null);
        assertThat(response)
                .extracting(r -> r.institutionId())
                .containsExactly(null, "test-citi", null);
        String json = new ObjectMapper().writeValueAsString(response);
        assertThat(json).doesNotContain("private-token", "accessToken", "plaidItemId", "item-a");
        verify(accounts, times(1)).findAll();
        verify(connections, times(1)).findConnectedItems();
    }

    @Test
    void exchangeAndReloadReturnSamePublicMetadataIncludingNullInstitution() {
        for (String institutionName : new String[] {"Chase", null}) {
            var account = account("a", "item-a");
            var item =
                    item("item-a", institutionName, institutionName == null ? null : "test-chase");
            var processor = mock(ExchangePlaidPublicTokenProcessor.class);
            when(processor.process(any()))
                    .thenReturn(new ExchangePlaidPublicTokenResult(item, List.of(account)));
            var controller =
                    new PlaidLinkController(
                            mock(CreatePlaidLinkTokenProcessor.class), processor, converter, query);
            var response =
                    controller.exchangePublicToken(
                            new ExchangePublicTokenRequest(
                                    "public-token",
                                    institutionName,
                                    item.institutionId(),
                                    List.of(
                                            new SelectedAccountRequest(
                                                    "a",
                                                    "Nickname",
                                                    "1234",
                                                    "depository",
                                                    "checking"))));
            when(accounts.findAll()).thenReturn(List.of(account));
            when(connections.findConnectedItems()).thenReturn(List.of(item));
            var reloaded =
                    new AccountController(new GetAccountsProcessor(query), converter).getAccounts();
            assertThat(response).isEqualTo(reloaded);
            var command = ArgumentCaptor.forClass(ExchangePlaidPublicTokenCommand.class);
            verify(processor).process(command.capture());
            assertThat(command.getValue().institutionId()).isEqualTo(item.institutionId());
        }
    }

    private AccountDO account(String id, String itemId) {
        return new AccountDO(id, itemId, "Nickname", "1234", "depository", "checking", true);
    }

    private PlaidItem item(String id, String name, String institutionId) {
        return new PlaidItem(
                id, "private-token", name, Instant.parse("2026-01-01T00:00:00Z"), institutionId);
    }
}
