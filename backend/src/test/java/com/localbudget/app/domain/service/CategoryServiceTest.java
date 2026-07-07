package com.localbudget.app.domain.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.localbudget.app.converter.CategoryConverter;
import com.localbudget.app.data.model.CategoryCsvRecord;
import com.localbudget.app.data.repository.CategoryCsvRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock private CategoryCsvRepository repository;

    private final CategoryConverter converter = new CategoryConverter();

    @Test
    void findAllReturnsCategoriesSortedBySortOrder() {
        when(repository.findAll())
                .thenReturn(
                        List.of(
                                category("shopping", "Shopping", false, "20"),
                                category("groceries", "Groceries", false, "10")));

        assertThat(newService().findAll())
                .extracting(category -> category.categoryId())
                .containsExactly("groceries", "shopping");
    }

    @Test
    void findActiveByIdRejectsBlankMissingAndArchivedCategories() {
        when(repository.findAll())
                .thenReturn(
                        List.of(
                                category("active", "Active", false, "10"),
                                category("archived", "Archived", true, "20")));

        CategoryService service = newService();

        assertThat(service.findActiveById("active")).isPresent();
        assertThat(service.findActiveById("archived")).isEmpty();
        assertThat(service.findActiveById("missing")).isEmpty();
        assertThat(service.findActiveById(" ")).isEmpty();
        assertThat(service.findActiveById(null)).isEmpty();
    }

    @Test
    void displayMethodsPreferAssignedIdThenLegacyThenPlaidThenUncategorized() {
        when(repository.findAll())
                .thenReturn(
                        List.of(
                                category("groceries", "Groceries", false, "10"),
                                category("uncategorized", "Uncategorized", false, "20")));
        CategoryService service = newService();

        assertThat(service.displayName("groceries")).isEqualTo("Groceries");
        assertThat(service.displayName("missing")).isEqualTo("Uncategorized");
        assertThat(service.displayCategoryForTransaction("groceries", "Legacy", "PLAID"))
                .isEqualTo("Groceries");
        assertThat(service.displayCategoryForTransaction(null, "Legacy", "PLAID"))
                .isEqualTo("Legacy");
        assertThat(service.displayCategoryForTransaction(null, null, "PLAID")).isEqualTo("PLAID");
        assertThat(service.displayCategoryForTransaction(null, null, null))
                .isEqualTo("Uncategorized");
    }

    @Test
    void byIdAndDisplayNamesByIdBuildLookupMaps() {
        when(repository.findAll())
                .thenReturn(
                        List.of(
                                category("groceries", "Groceries", false, "10"),
                                category("shopping", "Shopping", false, "20")));
        CategoryService service = newService();

        assertThat(service.byId()).containsKeys("groceries", "shopping");
        assertThat(service.displayNamesById())
                .containsEntry("groceries", "Groceries")
                .containsEntry("shopping", "Shopping");
    }

    private CategoryService newService() {
        return new CategoryService(repository, converter);
    }

    private CategoryCsvRecord category(
            String categoryId, String displayName, boolean archived, String sortOrder) {
        return new CategoryCsvRecord(
                categoryId,
                displayName,
                null,
                null,
                String.valueOf(archived),
                sortOrder,
                "2026-01-01T00:00:00Z",
                "2026-01-01T00:00:00Z");
    }
}
