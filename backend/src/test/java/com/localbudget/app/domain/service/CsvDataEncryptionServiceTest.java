package com.localbudget.app.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.domain.exception.CredentialOperationException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

class CsvDataEncryptionServiceTest {

    @TempDir Path dataDirectory;

    @Test
    void leavesCsvDataPlaintextUntilEncryptionIsEnabled() throws Exception {
        CsvDataEncryptionService service = newService();
        byte[] content = "header\nplain-value\n".getBytes(StandardCharsets.UTF_8);

        service.write("transactions.csv", content);

        assertThat(Files.readString(dataDirectory.resolve("transactions.csv")))
                .contains("plain-value");
        assertThat(dataDirectory.resolve("transactions.csv.enc")).doesNotExist();
        assertThat(service.isEnabled()).isFalse();
    }

    @Test
    void optInEncryptsExistingDataAndRequiresUnlock() throws Exception {
        CsvDataEncryptionService service = newService();
        byte[] content = "header\nsensitive-value\n".getBytes(StandardCharsets.UTF_8);
        service.write("transactions.csv", content);

        service.enable("a-secure-password".toCharArray());

        Path encrypted = dataDirectory.resolve("transactions.csv.enc");
        assertThat(encrypted).exists();
        assertThat(Files.readString(encrypted)).doesNotContain("sensitive-value");
        assertThat(dataDirectory.resolve("transactions.csv")).doesNotExist();
        assertThat(Files.list(dataDirectory))
                .anyMatch(
                        path -> path.getFileName().toString().startsWith("transactions.csv.bak-"));
        assertThat(service.read("transactions.csv")).isEqualTo(content);

        service.lock();
        assertThatThrownBy(() -> service.read("transactions.csv"))
                .isInstanceOf(CredentialOperationException.class)
                .hasMessageContaining("Unlock");

        service.unlock("a-secure-password".toCharArray(), true);
        assertThat(service.read("transactions.csv")).isEqualTo(content);
    }

    @Test
    void remembersEncryptionForEmptyDataDirectoryAfterRestart() {
        CsvDataEncryptionService service = newService();
        service.enable("a-secure-password".toCharArray());
        CsvDataEncryptionService restarted = newService();
        assertThat(restarted.isEnabled()).isTrue();
        assertThatThrownBy(() -> restarted.write("accounts.csv", new byte[] {1}))
                .isInstanceOf(CredentialOperationException.class);
        restarted.unlock("a-secure-password".toCharArray(), true);
        restarted.write("accounts.csv", new byte[] {1, 2, 3});
        assertThat(restarted.read("accounts.csv")).containsExactly(1, 2, 3);
        assertThat(dataDirectory.resolve("accounts.csv")).doesNotExist();
    }

    @Test
    void rejectsWrongPasswordWithoutChangingEncryptedData() throws Exception {
        CsvDataEncryptionService service = newService();
        service.enable("a-secure-password".toCharArray());
        service.write("transactions.csv", new byte[] {1, 2});
        byte[] original = Files.readAllBytes(dataDirectory.resolve("transactions.csv.enc"));
        CsvDataEncryptionService restarted = newService();
        assertThatThrownBy(() -> restarted.unlock("wrong-password".toCharArray(), true))
                .isInstanceOf(CredentialOperationException.class);
        assertThat(restarted.isUnlocked()).isFalse();
        assertThat(Files.readAllBytes(dataDirectory.resolve("transactions.csv.enc")))
                .isEqualTo(original);
        assertThatThrownBy(() -> restarted.unlock("a-secure-password".toCharArray(), false))
                .isInstanceOf(CredentialOperationException.class);
    }

    private CsvDataEncryptionService newService() {
        return new CsvDataEncryptionService(
                TestFixtures.properties(dataDirectory),
                new CredentialCryptoService(),
                new ObjectMapper(),
                Clock.fixed(Instant.parse("2026-09-05T00:00:00Z"), ZoneOffset.UTC));
    }
}
