package com.localbudget.app.domain.service;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.data.repository.EncryptedCredentialRepository;
import com.localbudget.app.data.repository.PlaintextCredentialRepository;
import com.localbudget.app.domain.exception.CredentialOperationException;
import com.localbudget.app.domain.exception.PlaidCredentialsUnavailableException;
import com.localbudget.app.domain.model.PlaidCredentials;
import com.localbudget.app.domain.model.SetupState;
import com.localbudget.app.domain.model.SetupStatus;
import java.time.Clock;
import java.time.Instant;
import java.util.Arrays;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class SetupService implements PlaidCredentialsProvider {

    public static final int MINIMUM_PASSWORD_LENGTH = 12;

    private final EncryptedCredentialRepository credentialRepository;
    private final PlaintextCredentialRepository plaintextCredentialRepository;
    private final CredentialCryptoService cryptoService;
    private final PlaidCredentialValidator credentialValidator;
    private final CsvDataEncryptionService csvDataEncryptionService;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final PlaidCredentials environmentCredentials;
    private volatile PlaidCredentials sessionCredentials;

    public SetupService(
            BudgetAppProperties properties,
            EncryptedCredentialRepository credentialRepository,
            PlaintextCredentialRepository plaintextCredentialRepository,
            CredentialCryptoService cryptoService,
            PlaidCredentialValidator credentialValidator,
            CsvDataEncryptionService csvDataEncryptionService,
            ObjectMapper objectMapper,
            Clock clock) {
        this.credentialRepository = credentialRepository;
        this.plaintextCredentialRepository = plaintextCredentialRepository;
        this.cryptoService = cryptoService;
        this.credentialValidator = credentialValidator;
        this.csvDataEncryptionService = csvDataEncryptionService;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.environmentCredentials = environmentCredentials(properties);
        this.sessionCredentials = passwordlessCredentials();
    }

    public SetupStatus status() {
        boolean encrypted = credentialRepository.exists();
        boolean dataAvailable =
                !csvDataEncryptionService.isEnabled() || csvDataEncryptionService.isUnlocked();
        SetupState state =
                sessionCredentials != null && dataAvailable
                        ? SetupState.READY
                        : encrypted ? SetupState.LOCKED : SetupState.NEEDS_SETUP;
        return new SetupStatus(
                state,
                encrypted,
                environmentCredentials != null,
                csvDataEncryptionService.isEnabled());
    }

    public void validate(PlaidCredentials credentials) {
        validateCredentialFields(credentials);
        credentialValidator.validate(credentials);
    }

    public synchronized void configure(
            PlaidCredentials credentials, char[] password, boolean encryptCsvData) {
        if (credentialRepository.exists() || plaintextCredentialRepository.exists()) {
            throw new CredentialOperationException(
                    "Setup is already saved. Continue with the existing setup.");
        }
        if (encryptCsvData) {
            validatePassword(password);
        } else if (csvDataEncryptionService.isEnabled()) {
            throw new CredentialOperationException(
                    "Existing encrypted data must be unlocked with its original setup.");
        }
        validate(credentials);
        if (!encryptCsvData) {
            plaintextCredentialRepository.write(credentials);
            sessionCredentials = credentials;
            return;
        }
        Instant now = Instant.now(clock);
        StoredPlaidCredentials stored =
                new StoredPlaidCredentials(
                        credentials.clientId(),
                        credentials.secret(),
                        credentials.environment(),
                        encryptCsvData,
                        now.toString(),
                        now.toString());
        byte[] plaintext = objectMapper.writeValueAsBytes(stored);
        try {
            credentialRepository.write(cryptoService.encrypt(plaintext, password));
            csvDataEncryptionService.enable(password);
            sessionCredentials = credentials;
        } catch (RuntimeException exception) {
            sessionCredentials = null;
            csvDataEncryptionService.lock();
            throw exception;
        } finally {
            Arrays.fill(plaintext, (byte) 0);
        }
    }

    public synchronized void unlock(char[] password) {
        if (!credentialRepository.exists()) {
            throw new CredentialOperationException("No saved credentials are available.");
        }
        byte[] plaintext = cryptoService.decrypt(credentialRepository.read(), password);
        try {
            StoredPlaidCredentials stored =
                    objectMapper.readValue(plaintext, StoredPlaidCredentials.class);
            PlaidCredentials credentials =
                    new PlaidCredentials(
                            stored.plaidClientId(),
                            stored.plaidSecret(),
                            stored.plaidEnvironment());
            validateCredentialFields(credentials);
            csvDataEncryptionService.unlock(password, stored.encryptCsvData());
            sessionCredentials = credentials;
        } catch (RuntimeException exception) {
            throw new CredentialOperationException("Unable to unlock saved credentials.");
        } finally {
            Arrays.fill(plaintext, (byte) 0);
        }
    }

    public synchronized void lock() {
        sessionCredentials = passwordlessCredentials();
        csvDataEncryptionService.lock();
    }

    private PlaidCredentials passwordlessCredentials() {
        if (environmentCredentials != null) {
            return environmentCredentials;
        }
        // Never fall back to plaintext when an encrypted setup is present.
        if (!credentialRepository.exists() && plaintextCredentialRepository.exists()) {
            PlaidCredentials credentials = plaintextCredentialRepository.read();
            validateCredentialFields(credentials);
            return credentials;
        }
        return null;
    }

    @Override
    public PlaidCredentials requireCredentials() {
        PlaidCredentials credentials = sessionCredentials;
        if (credentials == null || status().state() != SetupState.READY) {
            throw new PlaidCredentialsUnavailableException();
        }
        return credentials;
    }

    private void validateCredentialFields(PlaidCredentials credentials) {
        if (credentials == null
                || isBlank(credentials.clientId())
                || isBlank(credentials.secret())
                || credentials.environment() == null) {
            throw new CredentialOperationException(
                    "Plaid credentials and environment are required.");
        }
    }

    private void validatePassword(char[] password) {
        if (password == null || password.length < MINIMUM_PASSWORD_LENGTH) {
            throw new CredentialOperationException(
                    "Password must contain at least " + MINIMUM_PASSWORD_LENGTH + " characters.");
        }
    }

    private PlaidCredentials environmentCredentials(BudgetAppProperties properties) {
        BudgetAppProperties.PlaidConfig plaid = properties.plaid();
        if (isBlank(plaid.clientId()) || isBlank(plaid.secret())) {
            return null;
        }
        return new PlaidCredentials(plaid.clientId(), plaid.secret(), plaid.environment());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private record StoredPlaidCredentials(
            String plaidClientId,
            String plaidSecret,
            BudgetAppProperties.Environment plaidEnvironment,
            boolean encryptCsvData,
            String createdAt,
            String updatedAt) {}
}
