package com.localbudget.app.api.model.response;

import com.localbudget.app.domain.model.SetupStatus;
import java.util.Locale;

public record SetupStatusResponse(
        String state,
        boolean hasEncryptedSecrets,
        boolean hasEnvironmentCredentials,
        String csvEncryptionStatus) {

    public static SetupStatusResponse from(SetupStatus status) {
        return new SetupStatusResponse(
                status.state().name().toLowerCase(Locale.ROOT),
                status.hasEncryptedSecrets(),
                status.hasEnvironmentCredentials(),
                status.csvDataEncrypted() ? "encrypted" : "plaintext");
    }
}
