package com.localbudget.app.config;

import java.nio.file.Path;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "budget")
public record BudgetAppProperties(Path dataDirectory, PlaidConfig plaid) {
    public record PlaidConfig(
            Environment environment,
            String clientId,
            String secret,
            String clientName,
            List<String> products,
            List<String> countryCodes) {}

    public enum Environment {
        PROD,
        SANDBOX
    }
}
