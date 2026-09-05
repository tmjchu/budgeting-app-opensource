package com.localbudget.app.data.repository;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.domain.exception.CredentialOperationException;
import com.localbudget.app.domain.model.PlaidCredentials;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

@Repository
public class PlaintextCredentialRepository {

    private final Path path;
    private final ObjectMapper objectMapper;

    public PlaintextCredentialRepository(
            BudgetAppProperties properties, ObjectMapper objectMapper) {
        this.path = properties.dataDirectory().resolve("secrets.json");
        this.objectMapper = objectMapper;
    }

    public boolean exists() {
        return Files.isRegularFile(path);
    }

    public PlaidCredentials read() {
        try {
            return objectMapper.readValue(path.toFile(), PlaidCredentials.class);
        } catch (RuntimeException exception) {
            throw new CredentialOperationException("Unable to read saved credentials.");
        }
    }

    public void write(PlaidCredentials credentials) {
        Path temporaryPath = null;
        try {
            Files.createDirectories(path.getParent());
            temporaryPath = Files.createTempFile(path.getParent(), ".credentials-", ".tmp");
            objectMapper.writeValue(temporaryPath.toFile(), credentials);
            Files.move(temporaryPath, path, StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException | RuntimeException exception) {
            throw new CredentialOperationException("Unable to save local credentials.");
        } finally {
            if (temporaryPath != null) {
                try {
                    Files.deleteIfExists(temporaryPath);
                } catch (IOException ignored) {
                    // Preserve the original write failure.
                }
            }
        }
    }
}
