package com.localbudget.app.data.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.localbudget.app.TestFixtures;
import com.localbudget.app.converter.CategoryConverter;
import com.localbudget.app.data.model.CategoryCsvRecord;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CategoryCsvRepositoryTest {

    @TempDir Path dataDirectory;

    @Test
    void findAllSeedsDefaultCategoriesInOrder() {
        CategoryCsvRepository repository =
                new CategoryCsvRepository(
                        TestFixtures.properties(dataDirectory), new CategoryConverter());

        assertThat(repository.findAll())
                .extracting(CategoryCsvRecord::displayName)
                .containsExactly(
                        "Auto & Transport",
                        "Bills & Utilities",
                        "Business",
                        "Cash & Checks",
                        "Charitable Donations",
                        "Credit Card Payment",
                        "Dining & Drinks",
                        "Education",
                        "Entertainment & Rec.",
                        "Family Care",
                        "Fees",
                        "Gifts",
                        "Groceries",
                        "Health & Wellness",
                        "Home & Garden",
                        "Ignore",
                        "Income",
                        "Internal Transfers",
                        "Investment",
                        "Legal",
                        "Loan Payment",
                        "Medical",
                        "Personal Care",
                        "Pets",
                        "Reimbursement",
                        "Savings Transfer",
                        "Shopping",
                        "Software & Tech",
                        "Taxes",
                        "Travel & Vacation",
                        "Uncategorized");
        assertThat(repository.findAll())
                .extracting(CategoryCsvRecord::categoryId)
                .contains("auto-transport", "bills-utilities", "uncategorized");
    }

    @Test
    void writeAllSortsBySortOrderAndRoundTripsRecords() {
        CategoryCsvRepository repository =
                new CategoryCsvRepository(
                        TestFixtures.properties(dataDirectory), new CategoryConverter());

        repository.writeAll(
                List.of(
                        category("shopping", "Shopping", "20"),
                        category("groceries", "Groceries", "10")));

        assertThat(repository.findAll())
                .extracting(CategoryCsvRecord::categoryId)
                .containsExactly("groceries", "shopping");
        assertThat(repository.findAll())
                .filteredOn(record -> record.categoryId().equals("groceries"))
                .singleElement()
                .satisfies(
                        record -> {
                            assertThat(record.displayName()).isEqualTo("Groceries");
                            assertThat(record.sortOrder()).isEqualTo("10");
                        });
    }

    private CategoryCsvRecord category(String categoryId, String displayName, String sortOrder) {
        return new CategoryCsvRecord(
                categoryId,
                displayName,
                null,
                null,
                "false",
                sortOrder,
                "2026-01-01T00:00:00Z",
                "2026-01-01T00:00:00Z");
    }
}
