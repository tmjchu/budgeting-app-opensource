package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.request.AssignTransactionCategoryRequest;
import com.localbudget.app.api.model.request.UpdateTransactionsRequest;
import com.localbudget.app.api.model.response.TransactionResponse;
import com.localbudget.app.converter.TransactionConverter;
import com.localbudget.app.domain.model.TransactionView;
import com.localbudget.app.domain.model.command.AssignTransactionCategoryCommand;
import com.localbudget.app.domain.model.command.TransactionQueryCommand;
import com.localbudget.app.domain.model.command.UpdateTransactionsCommand;
import com.localbudget.app.domain.processor.AssignTransactionCategoryProcessor;
import com.localbudget.app.domain.processor.GetTransactionsProcessor;
import com.localbudget.app.domain.processor.UpdateTransactionsProcessor;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final GetTransactionsProcessor getTransactionsProcessor;
    private final AssignTransactionCategoryProcessor assignTransactionCategoryProcessor;
    private final UpdateTransactionsProcessor updateTransactionsProcessor;
    private final TransactionConverter transactionConverter;

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
        return getTransactionsProcessor.process(command).stream().map(this::toResponse).toList();
    }

    @PostMapping("/update")
    public List<TransactionResponse> updateTransactions(
            @Valid @RequestBody UpdateTransactionsRequest request) {
        UpdateTransactionsCommand command =
                new UpdateTransactionsCommand(
                        request.transactionIds(),
                        request.customName(),
                        request.categoryId(),
                        request.date());
        return updateTransactionsProcessor.process(command).stream().map(this::toResponse).toList();
    }

    /**
     * @deprecated Use {@code POST /api/transactions/update} instead.
     */
    @Deprecated(since = "0.1.0", forRemoval = false)
    @PatchMapping("/{transactionId}/category")
    public TransactionResponse assignCategory(
            @PathVariable String transactionId,
            @Valid @RequestBody AssignTransactionCategoryRequest request) {
        TransactionView transaction =
                assignTransactionCategoryProcessor.process(
                        new AssignTransactionCategoryCommand(transactionId, request.categoryId()));
        return toResponse(transaction);
    }

    private TransactionResponse toResponse(TransactionView transaction) {
        return transactionConverter.toResponse(
                transaction.transaction(), transaction.categoryDisplayName());
    }
}
