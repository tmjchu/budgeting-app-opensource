package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.model.TransactionView;
import com.localbudget.app.domain.model.command.TransactionQueryCommand;
import com.localbudget.app.domain.service.CategoryService;
import com.localbudget.app.domain.service.TransactionService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetTransactionsProcessor {

    private final TransactionService transactionService;
    private final CategoryService categoryService;

    public List<TransactionView> process(TransactionQueryCommand command) {
        return transactionService.find(command, categoryService.displayNamesById());
    }
}
