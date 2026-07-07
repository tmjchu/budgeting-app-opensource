package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.model.TransactionView;
import com.localbudget.app.domain.model.command.TransactionQueryCommand;
import com.localbudget.app.domain.service.CategoryService;
import com.localbudget.app.domain.service.TransactionService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GetTransactionsProcessor {

    private final TransactionService transactionService;
    private final CategoryService categoryService;

    public GetTransactionsProcessor(
            TransactionService transactionService, CategoryService categoryService) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
    }

    public List<TransactionView> handle(TransactionQueryCommand command) {
        return transactionService.find(command, categoryService.displayNamesById());
    }
}
