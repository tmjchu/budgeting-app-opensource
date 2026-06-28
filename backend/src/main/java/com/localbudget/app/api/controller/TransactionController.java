package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.response.TransactionResponse;
import com.localbudget.app.converter.TransactionConverter;
import com.localbudget.app.domain.handler.GetTransactionsHandler;
import com.localbudget.app.domain.model.command.TransactionQueryCommand;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final GetTransactionsHandler getTransactionsHandler;
    private final TransactionConverter transactionConverter;

    public TransactionController(
            GetTransactionsHandler getTransactionsHandler,
            TransactionConverter transactionConverter) {
        this.getTransactionsHandler = getTransactionsHandler;
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
        return getTransactionsHandler.handle(command).stream()
                .map(transactionConverter::toResponse)
                .toList();
    }
}
