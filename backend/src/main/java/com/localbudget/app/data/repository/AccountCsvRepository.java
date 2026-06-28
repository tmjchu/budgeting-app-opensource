package com.localbudget.app.data.repository;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.data.model.AccountCsvRecord;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Repository;

@Repository
public class AccountCsvRepository extends CsvSupport {

    private static final String FILE_NAME = "accounts.csv";
    private static final String[] HEADERS = {
        "account_id", "plaid_item_id", "name", "mask", "type", "subtype", "tracked"
    };

    public AccountCsvRepository(BudgetAppProperties properties) {
        super(properties);
    }

    public List<AccountCsvRecord> findAll() {
        return readRecords(FILE_NAME, HEADERS).stream()
                .map(
                        csvRecord ->
                                new AccountCsvRecord(
                                        value(csvRecord, "account_id"),
                                        value(csvRecord, "plaid_item_id"),
                                        value(csvRecord, "name"),
                                        value(csvRecord, "mask"),
                                        value(csvRecord, "type"),
                                        value(csvRecord, "subtype"),
                                        value(csvRecord, "tracked")))
                .toList();
    }

    public List<AccountCsvRecord> findTracked() {
        return findAll().stream().filter(csvRecord -> Boolean.parseBoolean(csvRecord.tracked())).toList();
    }

    public void upsertAll(List<AccountCsvRecord> accounts) {
        Map<String, AccountCsvRecord> merged = new LinkedHashMap<>();
        for (AccountCsvRecord existing : findAll()) {
            merged.put(existing.accountId(), existing);
        }
        for (AccountCsvRecord account : accounts) {
            merged.put(account.accountId(), account);
        }
        writeAll(new ArrayList<>(merged.values()));
    }

    private void writeAll(List<AccountCsvRecord> accounts) {
        accounts.sort(
                Comparator.comparing(AccountCsvRecord::plaidItemId)
                        .thenComparing(AccountCsvRecord::accountId));
        writeRows(
                FILE_NAME,
                HEADERS,
                accounts.stream()
                        .map(
                                account ->
                                        List.of(
                                                value(account.accountId()),
                                                value(account.plaidItemId()),
                                                value(account.name()),
                                                value(account.mask()),
                                                value(account.type()),
                                                value(account.subtype()),
                                                value(account.tracked())))
                        .toList());
    }
}
