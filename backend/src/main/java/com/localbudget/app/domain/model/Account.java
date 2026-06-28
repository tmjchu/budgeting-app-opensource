package com.localbudget.app.domain.model;

public record Account(
        String accountId,
        String plaidItemId,
        String name,
        String mask,
        String type,
        String subtype,
        boolean tracked) {}
