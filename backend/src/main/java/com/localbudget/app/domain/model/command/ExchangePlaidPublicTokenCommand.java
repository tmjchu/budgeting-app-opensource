package com.localbudget.app.domain.model.command;

import java.util.List;

public record ExchangePlaidPublicTokenCommand(
        String publicToken,
        String institutionName,
        String institutionId,
        List<SelectedAccountCommand> selectedAccounts) {}
