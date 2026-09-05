package com.localbudget.app.data.repository;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.domain.exception.CredentialOperationException;
import com.localbudget.app.domain.model.EncryptedCredentialEnvelope;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

@Repository
public class EncryptedCredentialRepository {

    private static final String FILE_NAME = "secrets.json.enc";

    private final Path path;
    private final ObjectMapper objectMapper;

    public EncryptedCredentialRepository(
            BudgetAppProperties properties, ObjectMapper objectMapper) {
        this.path = properties.dataDirectory().resolve(FILE_NAME);
        this.objectMapper = objectMapper;
    }

    public boolean exists() {
        return Files.isRegularFile(path);
    }

    public EncryptedCredentialEnvelope read() {
        try {
            return objectMapper.readValue(path.toFile(), EncryptedCredentialEnvelope.class);
        } catch (RuntimeException exception) {
            throw new CredentialOperationException("Unable to read saved credentials.", exception);
        }
    }

    public void write(EncryptedCredentialEnvelope envelope) {
        Path temporaryPath = path.resolveSibling(path.getFileName() + ".tmp");
        try {
            Files.createDirectories(path.getParent());
            objectMapper.writeValue(temporaryPath.toFile(), envelope);
            Files.move(
                    temporaryPath,
                    path,
                    StandardCopyOption.ATOMIC_MOVE,
                    StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException | RuntimeException exception) {
            try {
                Files.deleteIfExists(temporaryPath);
            } catch (IOException ignored) {
                // Preserve the original write failure.
            }
            throw new CredentialOperationException(
                    "Unable to save encrypted credentials.", exception);
        }
    }
}
