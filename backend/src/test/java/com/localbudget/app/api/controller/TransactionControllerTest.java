package com.localbudget.app.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.api.model.request.AssignTransactionCategoryRequest;
import com.localbudget.app.api.model.request.UpdateTransactionsRequest;
import com.localbudget.app.api.model.response.TransactionResponse;
import com.localbudget.app.converter.TransactionConverter;
import com.localbudget.app.domain.model.TransactionDO;
import com.localbudget.app.domain.model.TransactionView;
import com.localbudget.app.domain.model.command.AssignTransactionCategoryCommand;
import com.localbudget.app.domain.model.command.TransactionQueryCommand;
import com.localbudget.app.domain.model.command.UpdateTransactionsCommand;
import com.localbudget.app.domain.processor.AssignTransactionCategoryProcessor;
import com.localbudget.app.domain.processor.GetTransactionsProcessor;
import com.localbudget.app.domain.processor.UpdateTransactionsProcessor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionControllerTest {

    @Mock private GetTransactionsProcessor getTransactionsProcessor;
    @Mock private AssignTransactionCategoryProcessor assignTransactionCategoryProcessor;
    @Mock private UpdateTransactionsProcessor updateTransactionsProcessor;

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
        when(getTransactionsProcessor.process(
                        new TransactionQueryCommand(
                                YearMonth.parse("2026-06"), null, null, null, null)))
                .thenReturn(List.of(new TransactionView(transaction, "Groceries")));

        List<TransactionResponse> responses =
                newController().getTransactions(YearMonth.parse("2026-06"), null, null, null, null);

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
    void updateTransactionsDelegatesAndReturnsUpdatedTransactions() {
        TransactionDO transaction =
                TestFixtures.transaction(
                                "txn-1",
                                LocalDate.parse("2026-06-01"),
                                new BigDecimal("12.00"),
                                "FOOD_AND_DRINK")
                        .withLocalCategoryId("shopping")
                        .withCustomName("Custom coffee")
                        .withCustomDate(LocalDate.parse("2026-06-05"));
        UpdateTransactionsCommand command =
                new UpdateTransactionsCommand(
                        Set.of("txn-1"),
                        "Custom coffee",
                        "shopping",
                        LocalDate.parse("2026-06-05"));
        when(updateTransactionsProcessor.process(command))
                .thenReturn(List.of(new TransactionView(transaction, "Shopping")));

        List<TransactionResponse> responses =
                newController()
                        .updateTransactions(
                                new UpdateTransactionsRequest(
                                        new LinkedHashSet<>(List.of("txn-1")),
                                        "Custom coffee",
                                        "shopping",
                                        LocalDate.parse("2026-06-05")));

        assertThat(responses)
                .singleElement()
                .satisfies(
                        response -> {
                            assertThat(response.name()).isEqualTo("Custom coffee");
                            assertThat(response.date()).isEqualTo(LocalDate.parse("2026-06-05"));
                            assertThat(response.category()).isEqualTo("Shopping");
                            assertThat(response.assignedCategoryId()).isEqualTo("shopping");
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
        when(assignTransactionCategoryProcessor.process(
                        new AssignTransactionCategoryCommand("txn-1", "shopping")))
                .thenReturn(new TransactionView(transaction, "Shopping"));

        TransactionResponse response =
                newController()
                        .assignCategory("txn-1", new AssignTransactionCategoryRequest("shopping"));

        assertThat(response.category()).isEqualTo("Shopping");
        assertThat(response.assignedCategoryId()).isEqualTo("shopping");
        assertThat(response.assignedCategoryName()).isEqualTo("Shopping");
    }

    private TransactionController newController() {
        return new TransactionController(
                getTransactionsProcessor,
                assignTransactionCategoryProcessor,
                updateTransactionsProcessor,
                transactionConverter);
    }
}
