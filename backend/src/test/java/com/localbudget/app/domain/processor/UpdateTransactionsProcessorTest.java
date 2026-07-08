package com.localbudget.app.domain.processor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.converter.CategoryConverter;
import com.localbudget.app.data.repository.CategoryCsvRepository;
import com.localbudget.app.domain.model.TransactionDO;
import com.localbudget.app.domain.model.TransactionView;
import com.localbudget.app.domain.model.command.UpdateTransactionsCommand;
import com.localbudget.app.domain.service.CategoryService;
import com.localbudget.app.domain.service.TransactionService;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class UpdateTransactionsProcessorTest {

    @TempDir Path dataDirectory;

    @Mock private TransactionService transactionService;

    @Test
    void processUpdatesTransactionsWithKnownActiveCategory() {
        TransactionDO updated =
                TestFixtures.transaction(
                                "txn-1",
                                LocalDate.parse("2026-06-01"),
                                new BigDecimal("12.00"),
                                "FOOD_AND_DRINK")
                        .withLocalCategoryId("groceries");
        UpdateTransactionsCommand command =
                new UpdateTransactionsCommand(
                        Set.of("txn-1"),
                        "Custom coffee",
                        "groceries",
                        LocalDate.parse("2026-06-02"));
        when(transactionService.updateTransactions(command)).thenReturn(List.of(updated));
        when(transactionService.toView(eq(updated), anyMap()))
                .thenReturn(new TransactionView(updated, "Groceries"));

        List<TransactionView> result = newProcessor().process(command);

        assertThat(result)
                .singleElement()
                .satisfies(
                        view -> {
                            assertThat(view.transaction()).isEqualTo(updated);
                            assertThat(view.categoryDisplayName()).isEqualTo("Groceries");
                        });
    }

    @Test
    void processRejectsUnknownCategory() {
        UpdateTransactionsCommand command =
                new UpdateTransactionsCommand(Set.of("txn-1"), null, "not-real", null);

        assertThatThrownBy(() -> newProcessor().process(command))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Unknown category id");
    }

    @Test
    void processMapsMissingTransactionToNotFound() {
        UpdateTransactionsCommand command =
                new UpdateTransactionsCommand(Set.of("missing"), "Name", null, null);
        when(transactionService.updateTransactions(command))
                .thenThrow(new NoSuchElementException("Transaction not found: missing"));

        assertThatThrownBy(() -> newProcessor().process(command))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Transaction not found: missing");
    }

    private UpdateTransactionsProcessor newProcessor() {
        CategoryConverter converter = new CategoryConverter();
        return new UpdateTransactionsProcessor(
                new CategoryService(
                        new CategoryCsvRepository(
                                TestFixtures.properties(dataDirectory), converter),
                        converter),
                transactionService);
    }
}
