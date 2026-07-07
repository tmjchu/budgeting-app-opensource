package com.localbudget.app.domain.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CategoryDefaultsTest {

    @Test
    void defaultsReturnsConfiguredCategoriesWithStableIdsAndSortOrder() {
        assertThat(CategoryDefaults.defaults())
                .hasSize(31)
                .first()
                .satisfies(
                        category -> {
                            assertThat(category.categoryId()).isEqualTo("auto-transport");
                            assertThat(category.displayName()).isEqualTo("Auto & Transport");
                            assertThat(category.sortOrder()).isEqualTo(10);
                            assertThat(category.archived()).isFalse();
                        });
        assertThat(CategoryDefaults.defaults())
                .last()
                .satisfies(
                        category -> {
                            assertThat(category.categoryId()).isEqualTo("uncategorized");
                            assertThat(category.displayName()).isEqualTo("Uncategorized");
                            assertThat(category.sortOrder()).isEqualTo(310);
                        });
    }

    @Test
    void categoryIdForDisplayNameMatchesDefaultsCaseInsensitively() {
        assertThat(CategoryDefaults.categoryIdForDisplayName("Dining & Drinks"))
                .contains("dining-drinks");
        assertThat(CategoryDefaults.categoryIdForDisplayName("dining & drinks"))
                .contains("dining-drinks");
        assertThat(CategoryDefaults.categoryIdForDisplayName("Not A Default")).isEmpty();
    }

    @Test
    void categoryIdNormalizesDisplayNames() {
        assertThat(CategoryDefaults.categoryId("Entertainment & Rec."))
                .isEqualTo("entertainment-rec");
        assertThat(CategoryDefaults.categoryId("  Software & Tech  "))
                .isEqualTo("software-tech");
    }
}
