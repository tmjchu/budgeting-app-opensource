package com.localbudget.app.domain.model.command;

public record SelectedAccountCommand(
        String accountId,
        String name,
        String mask,
        String type,
        String subtype
) {
}
