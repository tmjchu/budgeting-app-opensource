package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.model.TransactionDO;
import com.localbudget.app.domain.model.TransactionView;
import com.localbudget.app.domain.model.command.AssignTransactionCategoryCommand;
import com.localbudget.app.domain.service.CategoryService;
import com.localbudget.app.domain.service.TransactionService;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AssignTransactionCategoryProcessor {

    private final CategoryService categoryService;
    private final TransactionService transactionService;

    public AssignTransactionCategoryProcessor(
            CategoryService categoryService, TransactionService transactionService) {
        this.categoryService = categoryService;
        this.transactionService = transactionService;
    }

    public TransactionView handle(AssignTransactionCategoryCommand command) {
        categoryService
                .findActiveById(command.categoryId())
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST, "Unknown category id."));
        try {
            TransactionDO transaction =
                    transactionService.updateLocalCategory(
                            command.transactionId(), command.categoryId());
            return transactionService.toView(transaction, categoryService.displayNamesById());
        } catch (NoSuchElementException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        }
    }
}
