package com.localbudget.app.api.model.response;

public record AccountResponse(
        String accountId,
        String name,
        String mask,
        String type,
        String subtype,
        boolean tracked
) {
}
