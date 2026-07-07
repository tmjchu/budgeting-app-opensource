package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.request.AssignTransactionCategoryRequest;
import com.localbudget.app.api.model.response.TransactionResponse;
import com.localbudget.app.converter.TransactionConverter;
import com.localbudget.app.domain.handler.AssignTransactionCategoryHandler;
import com.localbudget.app.domain.handler.GetTransactionsHandler;
import com.localbudget.app.domain.model.TransactionDO;
import com.localbudget.app.domain.model.command.AssignTransactionCategoryCommand;
import com.localbudget.app.domain.model.command.TransactionQueryCommand;
import com.localbudget.app.domain.service.CategoryService;
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

    private final GetTransactionsHandler getTransactionsHandler;
    private final AssignTransactionCategoryHandler assignTransactionCategoryHandler;
    private final TransactionConverter transactionConverter;
    private final CategoryService categoryService;

    public TransactionController(
            GetTransactionsHandler getTransactionsHandler,
            AssignTransactionCategoryHandler assignTransactionCategoryHandler,
            TransactionConverter transactionConverter,
            CategoryService categoryService) {
        this.getTransactionsHandler = getTransactionsHandler;
        this.assignTransactionCategoryHandler = assignTransactionCategoryHandler;
        this.transactionConverter = transactionConverter;
        this.categoryService = categoryService;
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
        return getTransactionsHandler.handle(command).stream().map(this::toResponse).toList();
    }

    @PatchMapping("/{transactionId}/category")
    public TransactionResponse assignCategory(
            @PathVariable String transactionId,
            @Valid @RequestBody AssignTransactionCategoryRequest request) {
        TransactionDO transaction =
                assignTransactionCategoryHandler.handle(
                        new AssignTransactionCategoryCommand(transactionId, request.categoryId()));
        return toResponse(transaction);
    }

    private TransactionResponse toResponse(TransactionDO transaction) {
        return transactionConverter.toResponse(
                transaction,
                categoryService.displayCategoryForTransaction(
                        transaction.localCategoryId(),
                        transaction.localCategory(),
                        transaction.primaryCategory()));
    }
}
