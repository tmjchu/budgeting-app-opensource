package com.localbudget.app.domain.service;

import com.localbudget.app.converter.TransactionConverter;
import com.localbudget.app.data.repository.TransactionCsvRepository;
import com.localbudget.app.domain.model.Transaction;
import com.localbudget.app.domain.model.result.TransactionMergeResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class TransactionMergeService {

    private final TransactionCsvRepository transactionRepository;
    private final TransactionConverter transactionConverter;

    public TransactionMergeService(
            TransactionCsvRepository transactionRepository,
            TransactionConverter transactionConverter) {
        this.transactionRepository = transactionRepository;
        this.transactionConverter = transactionConverter;
    }

    public TransactionMergeResult mergeIntoLocalStore(List<Transaction> fetchedTransactions) {
        Map<String, Transaction> merged = new LinkedHashMap<>();
        for (Transaction existing : findAll()) {
            merged.put(existing.transactionId(), existing);
        }

        int added = 0;
        int updated = 0;
        int unchanged = 0;
        for (Transaction fetched : fetchedTransactions) {
            Transaction existing = merged.get(fetched.transactionId());
            Transaction candidate = preserveLocalEdits(fetched, existing);
            if (existing == null) {
                added++;
            } else if (!Objects.equals(existing, candidate)) {
                updated++;
            } else {
                unchanged++;
            }
            merged.put(candidate.transactionId(), candidate);
        }

        transactionRepository.writeAll(merged.values().stream()
                .map(transactionConverter::toCsv)
                .toList());
        return new TransactionMergeResult(added, updated, unchanged);
    }

    public List<Transaction> findAll() {
        return transactionRepository.findAll().stream()
                .map(transactionConverter::fromCsv)
                .toList();
    }

    private Transaction preserveLocalEdits(Transaction fetched, Transaction existing) {
        if (existing == null) {
            return fetched;
        }
        return new Transaction(
                fetched.transactionId(),
                fetched.plaidItemId(),
                fetched.accountId(),
                fetched.accountName(),
                fetched.date(),
                fetched.name(),
                fetched.merchantName(),
                fetched.amount(),
                fetched.primaryCategory(),
                fetched.detailedCategory(),
                existing.localCategory(),
                fetched.pending(),
                existing.excluded(),
                fetched.paymentChannel());
    }
}
