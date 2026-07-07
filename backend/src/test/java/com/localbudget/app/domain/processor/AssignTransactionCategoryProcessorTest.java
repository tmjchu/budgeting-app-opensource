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
import com.localbudget.app.domain.model.command.AssignTransactionCategoryCommand;
import com.localbudget.app.domain.service.CategoryService;
import com.localbudget.app.domain.service.TransactionService;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

@ExtendWith(MockitoExtension.class)
class AssignTransactionCategoryProcessorTest {

    @TempDir Path dataDirectory;

    @Mock private TransactionService transactionService;

    @Test
    void handleAssignsKnownActiveCategory() {
        TransactionDO assigned =
                TestFixtures.transaction(
                                "txn-1",
                                LocalDate.parse("2026-06-01"),
                                new BigDecimal("12.00"),
                                "FOOD_AND_DRINK")
                        .withLocalCategoryId("groceries");
        when(transactionService.updateLocalCategory("txn-1", "groceries"))
                .thenReturn(assigned);
        when(transactionService.toView(eq(assigned), anyMap()))
                .thenReturn(new TransactionView(assigned, "Groceries"));
        CategoryService categoryService = newCategoryService();
        AssignTransactionCategoryProcessor processor =
                new AssignTransactionCategoryProcessor(categoryService, transactionService);

        TransactionView result =
                processor.handle(new AssignTransactionCategoryCommand("txn-1", "groceries"));

        assertThat(result.transaction().localCategoryId()).isEqualTo("groceries");
        assertThat(result.categoryDisplayName()).isEqualTo("Groceries");
    }

    @Test
    void handleRejectsUnknownCategory() {
        AssignTransactionCategoryProcessor processor =
                new AssignTransactionCategoryProcessor(newCategoryService(), transactionService);

        assertThatThrownBy(
                        () ->
                                processor.handle(
                                        new AssignTransactionCategoryCommand("txn-1", "not-real")))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Unknown category id");
    }

    private CategoryService newCategoryService() {
        CategoryConverter converter = new CategoryConverter();
        return new CategoryService(
                new CategoryCsvRepository(TestFixtures.properties(dataDirectory), converter),
                converter);
    }
}
