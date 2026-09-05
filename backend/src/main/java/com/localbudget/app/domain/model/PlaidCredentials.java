package com.localbudget.app.domain.model;

import com.localbudget.app.config.BudgetAppProperties.Environment;

public record PlaidCredentials(String clientId, String secret, Environment environment) {}
