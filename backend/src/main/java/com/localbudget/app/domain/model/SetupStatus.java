package com.localbudget.app.domain.model;

public record SetupStatus(
        SetupState state,
        boolean hasEncryptedSecrets,
        boolean hasEnvironmentCredentials,
        boolean csvDataEncrypted) {}
