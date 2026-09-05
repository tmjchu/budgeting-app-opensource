package com.localbudget.app.data.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.converter.PlaidItemConverter;
import com.localbudget.app.data.model.PlaidItemCsvRecord;
import com.localbudget.app.domain.exception.CredentialOperationException;
import com.localbudget.app.domain.service.CredentialCryptoService;
import com.localbudget.app.domain.service.CsvDataEncryptionService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.ObjectMapper;

class PlaidItemCsvRepositoryTest {
    @TempDir Path directory;

    @Test
    void oldFourColumnRowsRemainReadableWhenNewItemsAreSaved() throws Exception {
        Files.writeString(
                directory.resolve("plaid_items.csv"),
                "plaid_item_id,access_token,institution_name,created_at\n"
                        + "old,old-token,Chase,2026-01-01T00:00:00Z\n");
        PlaidItemCsvRepository repository =
                new PlaidItemCsvRepository(TestFixtures.properties(directory));
        PlaidItemCsvRecord old = repository.findAll().getFirst();
        assertThat(old.institutionId()).isNull();
        assertThat(old.institutionName()).isEqualTo("Chase");
        assertThat(old.createdAt()).isEqualTo("2026-01-01T00:00:00Z");
        PlaidItemCsvRecord added =
                new PlaidItemCsvRecord(
                        "new", "new-token", "Citi", "2026-02-01T00:00:00Z", "test-citi");
        repository.upsert(added);
        assertThat(repository.findAll()).containsExactly(old, added);
        PlaidItemConverter converter = new PlaidItemConverter();
        assertThat(converter.toCsv(converter.fromCsv(added))).isEqualTo(added);
        assertThat(converter.toCsv(converter.fromCsv(old))).isEqualTo(old);
        assertThat(Files.readString(directory.resolve("plaid_items.csv")))
                .startsWith(
                        "plaid_item_id,access_token,institution_name,created_at,institution_id");
    }

    @Test
    void institutionMetadataSurvivesEncryptionAndRestart() {
        var properties = TestFixtures.properties(directory);
        CsvDataEncryptionService encryption =
                new CsvDataEncryptionService(
                        properties,
                        new CredentialCryptoService(),
                        new ObjectMapper(),
                        Clock.systemUTC());
        encryption.enable("test-password".toCharArray());
        PlaidItemCsvRecord item =
                new PlaidItemCsvRecord(
                        "item", "test-token", "Citi", "2026-02-01T00:00:00Z", "test-citi");
        new PlaidItemCsvRepository(properties, encryption).upsert(item);
        assertThat(directory.resolve("plaid_items.csv")).doesNotExist();
        CsvDataEncryptionService restarted =
                new CsvDataEncryptionService(
                        properties,
                        new CredentialCryptoService(),
                        new ObjectMapper(),
                        Clock.systemUTC());
        PlaidItemCsvRepository repository = new PlaidItemCsvRepository(properties, restarted);
        assertThatThrownBy(repository::findAll).isInstanceOf(CredentialOperationException.class);
        restarted.unlock("test-password".toCharArray(), true);
        assertThat(repository.findAll()).containsExactly(item);
    }
}
