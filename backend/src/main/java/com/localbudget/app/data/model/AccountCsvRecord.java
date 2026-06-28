package com.localbudget.app.data.model;

public record AccountCsvRecord(
        String accountId,
        String plaidItemId,
        String name,
        String mask,
        String type,
        String subtype,
        String tracked) {}
