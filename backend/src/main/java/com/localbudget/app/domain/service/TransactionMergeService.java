package com.localbudget.app.domain.service;

import com.localbudget.app.converter.TransactionConverter;
import com.localbudget.app.data.repository.TransactionCsvRepository;
import com.localbudget.app.domain.model.TransactionDO;
import com.localbudget.app.domain.model.result.TransactionMergeResult;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
public class TransactionMergeService {

    private final TransactionCsvRepository transactionRepository;
    private final TransactionConverter transactionConverter;
    private final CategoryMappingService categoryMappingService;

    public TransactionMergeService(
            TransactionCsvRepository transactionRepository,
            TransactionConverter transactionConverter,
            CategoryMappingService categoryMappingService) {
        this.transactionRepository = transactionRepository;
        this.transactionConverter = transactionConverter;
        this.categoryMappingService = categoryMappingService;
    }

    public TransactionMergeResult mergeIntoLocalStore(List<TransactionDO> fetchedTransactions) {
        Map<String, TransactionDO> merged = new LinkedHashMap<>();
        for (TransactionDO existing : findAll()) {
            merged.put(existing.transactionId(), existing);
        }

        int added = 0;
        int updated = 0;
        int unchanged = 0;
        for (TransactionDO fetched : fetchedTransactions) {
            TransactionDO existing = merged.get(fetched.transactionId());
            TransactionDO candidate =
                    preserveLocalEdits(fetched, existing)
                            .withLocalCategoryIdIfUnassigned(
                                    categoryMappingService.defaultCategoryId(fetched));
            if (existing == null) {
                added++;
            } else if (!Objects.equals(existing, candidate)) {
                updated++;
            } else {
                unchanged++;
            }
            merged.put(candidate.transactionId(), candidate);
        }

        transactionRepository.writeAll(
                merged.values().stream().map(transactionConverter::toCsv).toList());
        return new TransactionMergeResult(added, updated, unchanged);
    }

    public List<TransactionDO> findAll() {
        return transactionRepository.findAll().stream().map(transactionConverter::fromCsv).toList();
    }

    public TransactionDO updateLocalCategory(String transactionId, String categoryId) {
        List<TransactionDO> transactions = findAll();
        boolean found =
                transactions.stream()
                        .anyMatch(transaction -> transaction.transactionId().equals(transactionId));
        if (!found) {
            throw new NoSuchElementException("Transaction not found: " + transactionId);
        }

        List<TransactionDO> updated =
                transactions.stream()
                        .map(
                                transaction ->
                                        transaction.transactionId().equals(transactionId)
                                                ? transaction.withLocalCategoryId(categoryId)
                                                : transaction)
                        .toList();
        transactionRepository.writeAll(updated.stream().map(transactionConverter::toCsv).toList());
        return updated.stream()
                .filter(transaction -> transaction.transactionId().equals(transactionId))
                .findFirst()
                .orElseThrow();
    }

    private TransactionDO preserveLocalEdits(TransactionDO fetched, TransactionDO existing) {
        if (existing == null) {
            return fetched;
        }
        return new TransactionDO(
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
                existing.localCategoryId(),
                fetched.pending(),
                existing.excluded(),
                fetched.paymentChannel());
    }
}
