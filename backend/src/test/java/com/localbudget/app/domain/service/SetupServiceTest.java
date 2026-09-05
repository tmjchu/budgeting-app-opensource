package com.localbudget.app.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.data.repository.EncryptedCredentialRepository;
import com.localbudget.app.data.repository.PlaintextCredentialRepository;
import com.localbudget.app.domain.exception.CredentialOperationException;
import com.localbudget.app.domain.exception.PlaidCredentialsUnavailableException;
import com.localbudget.app.domain.model.PlaidCredentials;
import com.localbudget.app.domain.model.SetupState;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

class SetupServiceTest {

    @TempDir Path dataDirectory;

    @Test
    void configuresLocksAndUnlocksEncryptedCredentials() throws Exception {
        PlaidCredentialValidator validator = mock(PlaidCredentialValidator.class);
        SetupService service = newService(properties("", ""), validator);
        PlaidCredentials credentials = credentials();

        assertThat(service.status().state()).isEqualTo(SetupState.NEEDS_SETUP);
        service.configure(credentials, "a-secure-password".toCharArray(), true);

        assertThat(service.status().state()).isEqualTo(SetupState.READY);
        assertThat(service.status().hasEncryptedSecrets()).isTrue();
        assertThat(service.requireCredentials()).isEqualTo(credentials);
        verify(validator).validate(credentials);
        String saved = Files.readString(dataDirectory.resolve("secrets.json.enc"));
        assertThat(saved)
                .doesNotContain(credentials.clientId())
                .doesNotContain(credentials.secret());

        service.lock();
        assertThat(service.status().state()).isEqualTo(SetupState.LOCKED);
        assertThatThrownBy(service::requireCredentials)
                .isInstanceOf(PlaidCredentialsUnavailableException.class);

        service.unlock("a-secure-password".toCharArray());
        assertThat(service.status().state()).isEqualTo(SetupState.READY);
        assertThat(service.requireCredentials()).isEqualTo(credentials);
    }

    @Test
    void wrongPasswordDoesNotUnlockCredentials() {
        SetupService service = newService(properties("", ""), mock(PlaidCredentialValidator.class));
        service.configure(credentials(), "a-secure-password".toCharArray(), true);
        service.lock();

        assertThatThrownBy(() -> service.unlock("the-wrong-password".toCharArray()))
                .isInstanceOf(CredentialOperationException.class)
                .hasMessage("Unable to unlock saved credentials.");
        assertThat(service.status().state()).isEqualTo(SetupState.LOCKED);
    }

    @Test
    void environmentCredentialsAreReadyAndCannotBeLocked() {
        SetupService service =
                newService(
                        properties("environment-client", "environment-secret"),
                        mock(PlaidCredentialValidator.class));

        assertThat(service.status().state()).isEqualTo(SetupState.READY);
        assertThat(service.status().hasEnvironmentCredentials()).isTrue();
        service.lock();
        assertThat(service.status().state()).isEqualTo(SetupState.READY);
    }

    @Test
    void rejectsShortPasswordsBeforePersisting() {
        SetupService service = newService(properties("", ""), mock(PlaidCredentialValidator.class));

        assertThatThrownBy(() -> service.configure(credentials(), "short".toCharArray(), true))
                .isInstanceOf(CredentialOperationException.class)
                .hasMessageContaining("12 characters");
        assertThat(service.status().hasEncryptedSecrets()).isFalse();
    }

    @Test
    void optOutPreservesExistingPlaintextAcrossRestart() throws Exception {
        Path csv = dataDirectory.resolve("transactions.csv");
        Files.writeString(csv, "header\nunchanged\n");
        SetupService service = newService(properties("", ""), mock(PlaidCredentialValidator.class));
        service.configure(credentials(), null, false);
        SetupService restarted =
                newService(properties("", ""), mock(PlaidCredentialValidator.class));
        assertThat(restarted.status().state()).isEqualTo(SetupState.READY);
        assertThat(restarted.requireCredentials()).isEqualTo(credentials());
        restarted.lock();
        assertThat(restarted.status().state()).isEqualTo(SetupState.READY);
        assertThat(restarted.status().hasEncryptedSecrets()).isFalse();
        assertThat(restarted.status().csvDataEncrypted()).isFalse();
        assertThat(Files.readString(csv)).isEqualTo("header\nunchanged\n");
        assertThat(dataDirectory.resolve("transactions.csv.enc")).doesNotExist();
        assertThat(dataDirectory.resolve("secrets.json.enc")).doesNotExist();
        assertThat(Files.readString(dataDirectory.resolve("secrets.json")))
                .contains(credentials().secret());
        assertThatThrownBy(() -> restarted.configure(credentials(), null, false))
                .isInstanceOf(CredentialOperationException.class);
    }

    @Test
    void optInRejectsMissingPasswordWithoutWritingAnyFiles() throws Exception {
        SetupService service = newService(properties("", ""), mock(PlaidCredentialValidator.class));
        assertThatThrownBy(() -> service.configure(credentials(), null, true))
                .isInstanceOf(CredentialOperationException.class);
        assertThat(service.status().state()).isEqualTo(SetupState.NEEDS_SETUP);
        try (var files = Files.list(dataDirectory)) {
            assertThat(files).isEmpty();
        }
    }

    @Test
    void optOutIgnoresAndNeverPersistsAnUnusedPassword() throws Exception {
        SetupService service = newService(properties("", ""), mock(PlaidCredentialValidator.class));
        service.configure(credentials(), "unused".toCharArray(), false);
        assertThat(service.status().state()).isEqualTo(SetupState.READY);
        assertThat(Files.readString(dataDirectory.resolve("secrets.json")))
                .doesNotContain("unused", "password");
    }

    @Test
    void optOutCannotReplaceOrDowngradeEncryptedSetup() throws Exception {
        SetupService service = newService(properties("", ""), mock(PlaidCredentialValidator.class));
        service.configure(credentials(), "a-secure-password".toCharArray(), true);
        byte[] saved = Files.readAllBytes(dataDirectory.resolve("secrets.json.enc"));
        assertThatThrownBy(() -> service.configure(credentials(), null, false))
                .isInstanceOf(CredentialOperationException.class);
        assertThat(dataDirectory.resolve("secrets.json")).doesNotExist();
        assertThat(Files.readAllBytes(dataDirectory.resolve("secrets.json.enc"))).isEqualTo(saved);
    }

    @Test
    void optOutCannotBypassExistingEncryptedCsvData() {
        var properties = properties("", "");
        new CsvDataEncryptionService(
                        properties,
                        new CredentialCryptoService(),
                        new ObjectMapper(),
                        Clock.systemUTC())
                .enable("a-secure-password".toCharArray());
        SetupService service = newService(properties, mock(PlaidCredentialValidator.class));
        assertThatThrownBy(() -> service.configure(credentials(), null, false))
                .isInstanceOf(CredentialOperationException.class);
        assertThat(dataDirectory.resolve("secrets.json")).doesNotExist();
    }

    @Test
    void legacyEncryptedCredentialsWithoutCsvEncryptionStillUnlock() {
        var properties = properties("", "");
        var mapper = new ObjectMapper();
        var crypto = new CredentialCryptoService();
        byte[] legacyPayload =
                """
                {"plaidClientId":"configured-client","plaidSecret":"configured-secret",
                 "plaidEnvironment":"SANDBOX","encryptCsvData":false}
                """
                        .getBytes(java.nio.charset.StandardCharsets.UTF_8);
        new EncryptedCredentialRepository(properties, mapper)
                .write(crypto.encrypt(legacyPayload, "a-secure-password".toCharArray()));
        new PlaintextCredentialRepository(properties, mapper).write(credentials());
        SetupService service = newService(properties, mock(PlaidCredentialValidator.class));
        assertThat(service.status().state()).isEqualTo(SetupState.LOCKED);
        service.unlock("a-secure-password".toCharArray());
        assertThat(service.requireCredentials()).isEqualTo(credentials());
        assertThat(service.status().csvDataEncrypted()).isFalse();
        service.lock();
        assertThat(service.status().state()).isEqualTo(SetupState.LOCKED);
    }

    @Test
    void optInSurvivesRestartAndBlocksEnvironmentBypass() {
        SetupService service = newService(properties("", ""), mock(PlaidCredentialValidator.class));
        service.configure(credentials(), "a-secure-password".toCharArray(), true);
        SetupService restarted =
                newService(
                        properties("env-client", "env-secret"),
                        mock(PlaidCredentialValidator.class));
        assertThat(restarted.status().state()).isEqualTo(SetupState.LOCKED);
        assertThatThrownBy(restarted::requireCredentials)
                .isInstanceOf(PlaidCredentialsUnavailableException.class);
        restarted.unlock("a-secure-password".toCharArray());
        assertThat(restarted.status().csvDataEncrypted()).isTrue();
        assertThat(restarted.status().state()).isEqualTo(SetupState.READY);
        restarted.lock();
        assertThat(restarted.status().state()).isEqualTo(SetupState.LOCKED);
        assertThatThrownBy(
                        () ->
                                restarted.configure(
                                        credentials(), "replacement-password".toCharArray(), false))
                .isInstanceOf(CredentialOperationException.class);
    }

    private SetupService newService(
            BudgetAppProperties properties, PlaidCredentialValidator validator) {
        ObjectMapper objectMapper = new ObjectMapper();
        EncryptedCredentialRepository repository =
                new EncryptedCredentialRepository(properties, objectMapper);
        CredentialCryptoService cryptoService = new CredentialCryptoService();
        Clock clock = Clock.fixed(Instant.parse("2026-08-29T00:00:00Z"), ZoneOffset.UTC);
        return new SetupService(
                properties,
                repository,
                new PlaintextCredentialRepository(properties, objectMapper),
                cryptoService,
                validator,
                new CsvDataEncryptionService(properties, cryptoService, objectMapper, clock),
                objectMapper,
                clock);
    }

    private BudgetAppProperties properties(String clientId, String secret) {
        return new BudgetAppProperties(
                dataDirectory,
                new BudgetAppProperties.PlaidConfig(
                        BudgetAppProperties.Environment.SANDBOX,
                        clientId,
                        secret,
                        "Open Budget",
                        List.of("transactions"),
                        List.of("US")));
    }

    private PlaidCredentials credentials() {
        return new PlaidCredentials(
                "configured-client", "configured-secret", BudgetAppProperties.Environment.SANDBOX);
    }
}
