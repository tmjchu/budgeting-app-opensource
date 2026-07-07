package com.localbudget.app.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.api.model.request.AssignTransactionCategoryRequest;
import com.localbudget.app.api.model.response.TransactionResponse;
import com.localbudget.app.converter.TransactionConverter;
import com.localbudget.app.domain.handler.AssignTransactionCategoryHandler;
import com.localbudget.app.domain.handler.GetTransactionsHandler;
import com.localbudget.app.domain.model.TransactionDO;
import com.localbudget.app.domain.model.command.AssignTransactionCategoryCommand;
import com.localbudget.app.domain.model.command.TransactionQueryCommand;
import com.localbudget.app.domain.service.CategoryService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock private GetTransactionsHandler getTransactionsHandler;
    @Mock private AssignTransactionCategoryHandler assignTransactionCategoryHandler;
    @Mock private CategoryService categoryService;

    private final TransactionConverter transactionConverter = new TransactionConverter();

    @Test
    void getTransactionsReturnsDisplayCategoryAndAssignedFields() {
        TransactionDO transaction =
                TestFixtures.transaction(
                                "txn-1",
                                LocalDate.parse("2026-06-01"),
                                new BigDecimal("12.00"),
                                "FOOD_AND_DRINK")
                        .withLocalCategoryId("groceries");
        when(getTransactionsHandler.handle(
                        new TransactionQueryCommand(
                                YearMonth.parse("2026-06"), null, null, null, null)))
                .thenReturn(List.of(transaction));
        when(categoryService.displayCategoryForTransaction("groceries", null, "FOOD_AND_DRINK"))
                .thenReturn("Groceries");

        List<TransactionResponse> responses =
                newController()
                        .getTransactions(YearMonth.parse("2026-06"), null, null, null, null);

        assertThat(responses)
                .singleElement()
                .satisfies(
                        response -> {
                            assertThat(response.transactionId()).isEqualTo("txn-1");
                            assertThat(response.category()).isEqualTo("Groceries");
                            assertThat(response.assignedCategoryId()).isEqualTo("groceries");
                            assertThat(response.assignedCategoryName()).isEqualTo("Groceries");
                            assertThat(response.primaryCategory()).isEqualTo("FOOD_AND_DRINK");
                        });
    }

    @Test
    void assignCategoryDelegatesAndReturnsUpdatedTransaction() {
        TransactionDO transaction =
                TestFixtures.transaction(
                                "txn-1",
                                LocalDate.parse("2026-06-01"),
                                new BigDecimal("12.00"),
                                "FOOD_AND_DRINK")
                        .withLocalCategoryId("shopping");
        when(assignTransactionCategoryHandler.handle(
                        new AssignTransactionCategoryCommand("txn-1", "shopping")))
                .thenReturn(transaction);
        when(categoryService.displayCategoryForTransaction("shopping", null, "FOOD_AND_DRINK"))
                .thenReturn("Shopping");

        TransactionResponse response =
                newController()
                        .assignCategory(
                                "txn-1", new AssignTransactionCategoryRequest("shopping"));

        assertThat(response.category()).isEqualTo("Shopping");
        assertThat(response.assignedCategoryId()).isEqualTo("shopping");
        assertThat(response.assignedCategoryName()).isEqualTo("Shopping");
    }

    private TransactionController newController() {
        return new TransactionController(
                getTransactionsHandler,
                assignTransactionCategoryHandler,
                transactionConverter,
                categoryService);
    }
}
