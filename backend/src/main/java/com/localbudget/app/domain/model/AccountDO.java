package com.localbudget.app.domain.model;

public record AccountDO(
        String accountId,
        String plaidItemId,
        String name,
        String mask,
        String type,
        String subtype,
        boolean tracked) {}
