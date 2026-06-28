package com.localbudget.app.data.model;

public record PlaidItemCsvRecord(
        String plaidItemId,
        String accessToken,
        String institutionName,
        String createdAt
) {
}
