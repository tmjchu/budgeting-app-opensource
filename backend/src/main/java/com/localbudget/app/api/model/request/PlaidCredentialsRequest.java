package com.localbudget.app.api.model.request;

import com.localbudget.app.config.BudgetAppProperties.Environment;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record PlaidCredentialsRequest(
        @NotBlank String clientId, @NotBlank String secret, @NotNull Environment environment) {}
