package com.localbudget.app.api.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.localbudget.app.domain.model.CategoryDO;
import com.localbudget.app.domain.service.CategoryService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CategoryControllerTest {

    @Mock private CategoryService categoryService;

    @Test
    void getCategoriesReturnsCategoryResponses() {
        when(categoryService.findAll())
                .thenReturn(
                        List.of(
                                new CategoryDO(
                                        "groceries",
                                        "Groceries",
                                        "#00aa00",
                                        "food",
                                        false,
                                        10,
                                        Instant.parse("2026-01-01T00:00:00Z"),
                                        Instant.parse("2026-01-02T00:00:00Z"))));

        assertThat(new CategoryController(categoryService).getCategories())
                .singleElement()
                .satisfies(
                        response -> {
                            assertThat(response.categoryId()).isEqualTo("groceries");
                            assertThat(response.displayName()).isEqualTo("Groceries");
                            assertThat(response.color()).isEqualTo("#00aa00");
                            assertThat(response.parentCategoryId()).isEqualTo("food");
                            assertThat(response.archived()).isFalse();
                            assertThat(response.sortOrder()).isEqualTo(10);
                        });
    }
}
