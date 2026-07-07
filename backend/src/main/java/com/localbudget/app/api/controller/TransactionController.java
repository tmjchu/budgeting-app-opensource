package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.request.AssignTransactionCategoryRequest;
import com.localbudget.app.api.model.response.TransactionResponse;
import com.localbudget.app.converter.TransactionConverter;
import com.localbudget.app.domain.model.TransactionView;
import com.localbudget.app.domain.model.command.AssignTransactionCategoryCommand;
import com.localbudget.app.domain.model.command.TransactionQueryCommand;
import com.localbudget.app.domain.processor.AssignTransactionCategoryProcessor;
import com.localbudget.app.domain.processor.GetTransactionsProcessor;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final GetTransactionsProcessor getTransactionsProcessor;
    private final AssignTransactionCategoryProcessor assignTransactionCategoryProcessor;
    private final TransactionConverter transactionConverter;

    public TransactionController(
            GetTransactionsProcessor getTransactionsProcessor,
            AssignTransactionCategoryProcessor assignTransactionCategoryProcessor,
            TransactionConverter transactionConverter) {
        this.getTransactionsProcessor = getTransactionsProcessor;
        this.assignTransactionCategoryProcessor = assignTransactionCategoryProcessor;
        this.transactionConverter = transactionConverter;
    }

    @GetMapping
    public List<TransactionResponse> getTransactions(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
                    LocalDate endDate,
            @RequestParam(required = false) String accountId,
            @RequestParam(required = false) String category) {
        TransactionQueryCommand command =
                new TransactionQueryCommand(month, startDate, endDate, accountId, category);
        return getTransactionsProcessor.handle(command).stream().map(this::toResponse).toList();
    }

    @PatchMapping("/{transactionId}/category")
    public TransactionResponse assignCategory(
            @PathVariable String transactionId,
            @Valid @RequestBody AssignTransactionCategoryRequest request) {
        TransactionView transaction =
                assignTransactionCategoryProcessor.handle(
                        new AssignTransactionCategoryCommand(transactionId, request.categoryId()));
        return toResponse(transaction);
    }

    private TransactionResponse toResponse(TransactionView transaction) {
        return transactionConverter.toResponse(
                transaction.transaction(), transaction.categoryDisplayName());
    }
}
