package com.localbudget.app.domain.service;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.domain.exception.CredentialOperationException;
import com.localbudget.app.domain.model.EncryptedCredentialEnvelope;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

@Service
public class CsvDataEncryptionService {

    private static final String MARKER = ".csv-encryption.enc";

    private static final List<String> CSV_FILES =
            List.of(
                    "accounts.csv",
                    "plaid_items.csv",
                    "categories.csv",
                    "transactions.csv",
                    "balance_snapshots.csv",
                    "sync_runs.csv");

    private final Path dataDirectory;
    private final CredentialCryptoService cryptoService;
    private final ObjectMapper objectMapper;
    private final Clock clock;
    private volatile char[] sessionPassword;
    private volatile boolean enabled;

    public CsvDataEncryptionService(
            BudgetAppProperties properties,
            CredentialCryptoService cryptoService,
            ObjectMapper objectMapper,
            Clock clock) {
        this.dataDirectory = properties.dataDirectory();
        this.cryptoService = cryptoService;
        this.objectMapper = objectMapper;
        this.clock = clock;
        this.enabled = Files.exists(dataDirectory.resolve(MARKER)) || hasEncryptedFiles();
    }

    public boolean isEnabled() {
        return enabled;
    }

    public boolean isUnlocked() {
        return sessionPassword != null;
    }

    public boolean exists(String fileName) {
        return Files.exists(plainPath(fileName)) || Files.exists(encryptedPath(fileName));
    }

    public synchronized byte[] read(String fileName) {
        if (enabled) {
            requirePassword();
        }
        Path encrypted = encryptedPath(fileName);
        if (Files.exists(encrypted)) {
            return decrypt(encrypted, requirePassword());
        }
        try {
            Path plain = plainPath(fileName);
            return Files.exists(plain) ? Files.readAllBytes(plain) : null;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read " + plainPath(fileName), exception);
        }
    }

    public synchronized void write(String fileName, byte[] content) {
        if (enabled) {
            writeEncrypted(encryptedPath(fileName), content, requirePassword());
            return;
        }
        writePlain(plainPath(fileName), content);
    }

    public synchronized void enable(char[] password) {
        List<PendingEncryption> pending = new ArrayList<>();
        try {
            Files.createDirectories(dataDirectory);
            // Verify an existing encryption key before migrating or replacing anything.
            verifyEncryptedFiles(password);
            writeEncrypted(dataDirectory.resolve(MARKER), new byte[] {1}, password);
            enabled = true;
            for (String fileName : CSV_FILES) {
                Path plain = plainPath(fileName);
                if (!Files.isRegularFile(plain)) {
                    continue;
                }
                byte[] content = Files.readAllBytes(plain);
                Path temporary = encryptedPath(fileName).resolveSibling(fileName + ".enc.tmp");
                pending.add(new PendingEncryption(plain, encryptedPath(fileName), temporary));
                try {
                    if (Files.exists(encryptedPath(fileName))) {
                        byte[] previous = decrypt(encryptedPath(fileName), password);
                        try {
                            if (!Arrays.equals(content, previous)) {
                                throw new CredentialOperationException(
                                        "Plaintext and encrypted local data differ. Restore the"
                                                + " intended files before continuing.");
                            }
                        } finally {
                            Arrays.fill(previous, (byte) 0);
                        }
                    }
                    writeEncrypted(temporary, content, password);
                    byte[] verified = decrypt(temporary, password);
                    try {
                        if (!Arrays.equals(content, verified)) {
                            throw new CredentialOperationException(
                                    "Unable to verify encrypted local data.");
                        }
                    } finally {
                        Arrays.fill(verified, (byte) 0);
                    }
                } finally {
                    Arrays.fill(content, (byte) 0);
                }
            }

            String backupSuffix =
                    ".bak-" + Instant.now(clock).toEpochMilli() + "-" + UUID.randomUUID();
            for (PendingEncryption item : pending) {
                Path backup =
                        item.plain().resolveSibling(item.plain().getFileName() + backupSuffix);
                Files.copy(item.plain(), backup);
                Files.move(
                        item.temporary(),
                        item.encrypted(),
                        StandardCopyOption.ATOMIC_MOVE,
                        StandardCopyOption.REPLACE_EXISTING);
                Files.delete(item.plain());
            }
            setSession(password, true);
        } catch (IOException | RuntimeException exception) {
            lock();
            for (PendingEncryption item : pending) {
                try {
                    Files.deleteIfExists(item.temporary());
                } catch (IOException ignored) {
                    // Preserve the migration failure.
                }
            }
            if (exception instanceof CredentialOperationException credentialException) {
                throw credentialException;
            }
            throw new CredentialOperationException("Unable to encrypt local CSV data.", exception);
        }
    }

    public synchronized void unlock(char[] password, boolean encryptionEnabled) {
        if (!encryptionEnabled) {
            if (enabled || hasEncryptedFiles()) {
                throw new CredentialOperationException(
                        "Encrypted local data must be unlocked with its original setup.");
            }
            lock();
            enabled = false;
            return;
        }
        // This also resumes a migration interrupted after credentials were saved.
        enable(password);
    }

    private void verifyEncryptedFiles(char[] password) {
        List<Path> files = new ArrayList<>(CSV_FILES.stream().map(this::encryptedPath).toList());
        files.add(dataDirectory.resolve(MARKER));
        for (Path file : files) {
            if (Files.isRegularFile(file)) {
                byte[] verified = decrypt(file, password);
                Arrays.fill(verified, (byte) 0);
            }
        }
    }

    public synchronized void lock() {
        if (sessionPassword != null) {
            Arrays.fill(sessionPassword, '\0');
            sessionPassword = null;
        }
    }

    private void setSession(char[] password, boolean encryptionEnabled) {
        lock();
        sessionPassword = password.clone();
        enabled = encryptionEnabled;
    }

    private char[] requirePassword() {
        char[] password = sessionPassword;
        if (password == null) {
            throw new CredentialOperationException(
                    "Unlock the app to access encrypted local data.");
        }
        return password;
    }

    private void writeEncrypted(Path path, byte[] content, char[] password) {
        Path temporary = null;
        try {
            Files.createDirectories(path.getParent());
            EncryptedCredentialEnvelope envelope = cryptoService.encrypt(content, password);
            temporary = Files.createTempFile(path.getParent(), ".encrypted-write-", ".tmp");
            objectMapper.writeValue(temporary.toFile(), envelope);
            Files.move(
                    temporary,
                    path,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException exception) {
            throw new CredentialOperationException("Unable to encrypt local CSV data.", exception);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                    // Only encrypted temporary content may remain after a cleanup failure.
                }
            }
        }
    }

    private byte[] decrypt(Path path, char[] password) {
        try {
            return cryptoService.decrypt(
                    objectMapper.readValue(path.toFile(), EncryptedCredentialEnvelope.class),
                    password);
        } catch (RuntimeException exception) {
            throw new CredentialOperationException("Unable to decrypt local CSV data.");
        }
    }

    private void writePlain(Path path, byte[] content) {
        try {
            Files.createDirectories(path.getParent());
            Files.write(path, content);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write " + path, exception);
        }
    }

    private boolean hasEncryptedFiles() {
        return CSV_FILES.stream().map(this::encryptedPath).anyMatch(Files::isRegularFile);
    }

    private Path plainPath(String fileName) {
        return dataDirectory.resolve(fileName);
    }

    private Path encryptedPath(String fileName) {
        return dataDirectory.resolve(fileName + ".enc");
    }

    private record PendingEncryption(Path plain, Path encrypted, Path temporary) {}
}
