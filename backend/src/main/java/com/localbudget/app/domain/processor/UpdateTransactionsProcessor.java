package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.model.TransactionDO;
import com.localbudget.app.domain.model.TransactionView;
import com.localbudget.app.domain.model.command.UpdateTransactionsCommand;
import com.localbudget.app.domain.service.CategoryService;
import com.localbudget.app.domain.service.TransactionService;
import io.micrometer.common.util.StringUtils;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class UpdateTransactionsProcessor {

    private final CategoryService categoryService;
    private final TransactionService transactionService;

    public List<TransactionView> process(UpdateTransactionsCommand command) {

        var categoriesMap = categoryService.displayNamesById();

        if (StringUtils.isNotBlank(command.categoryId())
                && !categoriesMap.containsKey(command.categoryId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unknown category id.");
        }

        try {
            List<TransactionDO> transactions = transactionService.updateTransactions(command);

            return transactions.stream()
                    .map(transaction -> transactionService.toView(transaction, categoriesMap))
                    .toList();
        } catch (NoSuchElementException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, exception.getMessage(), exception);
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, exception.getMessage(), exception);
        }
    }
}
