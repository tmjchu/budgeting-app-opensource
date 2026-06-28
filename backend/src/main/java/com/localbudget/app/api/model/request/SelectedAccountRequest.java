package com.localbudget.app.api.model.request;

import jakarta.validation.constraints.NotBlank;

public record SelectedAccountRequest(
        @NotBlank String accountId, String name, String mask, String type, String subtype) {}
