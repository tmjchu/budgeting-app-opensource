package com.localbudget.app.data.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.data.model.AccountCsvRecord;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AccountCsvRepositoryTest {

    @TempDir Path dataDirectory;

    @Test
    void upsertAllCreatesCsvAndReplacesExistingAccountsByAccountId() {
        AccountCsvRepository repository =
                new AccountCsvRepository(TestFixtures.properties(dataDirectory));

        repository.upsertAll(
                List.of(
                        new AccountCsvRecord(
                                "acc-1",
                                "item-1",
                                "Checking",
                                "1111",
                                "depository",
                                "checking",
                                "true")));
        repository.upsertAll(
                List.of(
                        new AccountCsvRecord(
                                "acc-1",
                                "item-1",
                                "Renamed Checking",
                                "1111",
                                "depository",
                                "checking",
                                "true"),
                        new AccountCsvRecord(
                                "acc-2",
                                "item-1",
                                "Savings",
                                "2222",
                                "depository",
                                "savings",
                                "false")));

        assertThat(repository.findAll())
                .extracting(AccountCsvRecord::name)
                .containsExactly("Renamed Checking", "Savings");
        assertThat(repository.findTracked())
                .extracting(AccountCsvRecord::accountId)
                .containsExactly("acc-1");
    }
}
