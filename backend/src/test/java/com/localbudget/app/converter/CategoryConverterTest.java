package com.localbudget.app.converter;

import static org.assertj.core.api.Assertions.assertThat;

import com.localbudget.app.data.model.CategoryCsvRecord;
import com.localbudget.app.domain.model.CategoryDO;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class CategoryConverterTest {

    private final CategoryConverter converter = new CategoryConverter();

    @Test
    void fromCsvMapsFieldsAndParsesBlankDefaults() {
        CategoryDO category =
                converter.fromCsv(
                        new CategoryCsvRecord(
                                "groceries",
                                "Groceries",
                                "#00aa00",
                                "parent",
                                "true",
                                "",
                                "",
                                "2026-06-01T00:00:00Z"));

        assertThat(category.categoryId()).isEqualTo("groceries");
        assertThat(category.displayName()).isEqualTo("Groceries");
        assertThat(category.color()).isEqualTo("#00aa00");
        assertThat(category.parentCategoryId()).isEqualTo("parent");
        assertThat(category.archived()).isTrue();
        assertThat(category.sortOrder()).isZero();
        assertThat(category.createdAt()).isEqualTo(Instant.EPOCH);
        assertThat(category.updatedAt()).isEqualTo(Instant.parse("2026-06-01T00:00:00Z"));
    }

    @Test
    void toCsvMapsDomainObject() {
        CategoryCsvRecord record =
                converter.toCsv(
                        new CategoryDO(
                                "shopping",
                                "Shopping",
                                null,
                                null,
                                false,
                                270,
                                Instant.parse("2026-01-01T00:00:00Z"),
                                Instant.parse("2026-01-02T00:00:00Z")));

        assertThat(record.categoryId()).isEqualTo("shopping");
        assertThat(record.displayName()).isEqualTo("Shopping");
        assertThat(record.archived()).isEqualTo("false");
        assertThat(record.sortOrder()).isEqualTo("270");
        assertThat(record.createdAt()).isEqualTo("2026-01-01T00:00:00Z");
        assertThat(record.updatedAt()).isEqualTo("2026-01-02T00:00:00Z");
    }
}
