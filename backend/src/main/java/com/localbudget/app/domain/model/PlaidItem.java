package com.localbudget.app.domain.model;

import java.time.Instant;

public record PlaidItem(
        String plaidItemId,
        String accessToken,
        String institutionName,
        Instant createdAt
) {
}
