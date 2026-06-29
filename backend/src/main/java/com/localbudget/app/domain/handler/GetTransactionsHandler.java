package com.localbudget.app.domain.handler;

import com.localbudget.app.domain.model.TransactionDO;
import com.localbudget.app.domain.model.command.TransactionQueryCommand;
import com.localbudget.app.domain.service.TransactionQueryService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GetTransactionsHandler {

    private final TransactionQueryService transactionQueryService;

    public GetTransactionsHandler(TransactionQueryService transactionQueryService) {
        this.transactionQueryService = transactionQueryService;
    }

    public List<TransactionDO> handle(TransactionQueryCommand command) {
        return transactionQueryService.find(command);
    }
}
