package com.localbudget.app.domain.service;

import com.localbudget.app.domain.model.Transaction;
import com.localbudget.app.domain.model.command.TransactionQueryCommand;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class TransactionQueryService {

    private final TransactionMergeService transactionMergeService;

    public TransactionQueryService(TransactionMergeService transactionMergeService) {
        this.transactionMergeService = transactionMergeService;
    }

    public List<Transaction> find(TransactionQueryCommand command) {
        LocalDate start = resolveStart(command);
        LocalDate end = resolveEnd(command);
        return transactionMergeService.findAll().stream()
                .filter(transaction -> !transaction.date().isBefore(start))
                .filter(transaction -> !transaction.date().isAfter(end))
                .filter(transaction -> command.accountId() == null
                        || command.accountId().isBlank()
                        || command.accountId().equals(transaction.accountId()))
                .filter(transaction -> command.category() == null
                        || command.category().isBlank()
                        || command.category().equalsIgnoreCase(transaction.effectiveCategory()))
                .sorted(Comparator.comparing(Transaction::date).reversed()
                        .thenComparing(Transaction::transactionId))
                .toList();
    }

    public List<Transaction> findAll() {
        return transactionMergeService.findAll();
    }

    private static LocalDate resolveStart(TransactionQueryCommand command) {
        if (command.startDate() != null) {
            return command.startDate();
        }
        YearMonth month = command.month() == null ? YearMonth.now() : command.month();
        return month.atDay(1);
    }

    private static LocalDate resolveEnd(TransactionQueryCommand command) {
        if (command.endDate() != null) {
            return command.endDate();
        }
        YearMonth month = command.month() == null ? YearMonth.now() : command.month();
        return month.atEndOfMonth();
    }
}
