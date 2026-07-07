package com.localbudget.app.domain.handler;

import com.localbudget.app.domain.model.TransactionDO;
import com.localbudget.app.domain.model.command.AssignTransactionCategoryCommand;
import com.localbudget.app.domain.service.CategoryService;
import com.localbudget.app.domain.service.TransactionMergeService;
import java.util.NoSuchElementException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
public class AssignTransactionCategoryHandler {

    private final CategoryService categoryService;
    private final TransactionMergeService transactionMergeService;

    public AssignTransactionCategoryHandler(
            CategoryService categoryService, TransactionMergeService transactionMergeService) {
        this.categoryService = categoryService;
        this.transactionMergeService = transactionMergeService;
    }

    public TransactionDO handle(AssignTransactionCategoryCommand command) {
        categoryService
                .findActiveById(command.categoryId())
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.BAD_REQUEST, "Unknown category id."));
        try {
            return transactionMergeService.updateLocalCategory(
                    command.transactionId(), command.categoryId());
        } catch (NoSuchElementException exception) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        }
    }
}
