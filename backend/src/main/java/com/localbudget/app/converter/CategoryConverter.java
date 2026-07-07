package com.localbudget.app.converter;

import com.localbudget.app.data.model.CategoryCsvRecord;
import com.localbudget.app.domain.model.CategoryDO;
import java.time.Instant;
import org.springframework.stereotype.Component;

@Component
public class CategoryConverter {

    public CategoryDO fromCsv(CategoryCsvRecord record) {
        return new CategoryDO(
                record.categoryId(),
                record.displayName(),
                record.color(),
                record.parentCategoryId(),
                Boolean.parseBoolean(record.archived()),
                parseSortOrder(record.sortOrder()),
                parseInstant(record.createdAt()),
                parseInstant(record.updatedAt()));
    }

    public CategoryCsvRecord toCsv(CategoryDO category) {
        return new CategoryCsvRecord(
                category.categoryId(),
                category.displayName(),
                category.color(),
                category.parentCategoryId(),
                String.valueOf(category.archived()),
                String.valueOf(category.sortOrder()),
                category.createdAt().toString(),
                category.updatedAt().toString());
    }

    private static int parseSortOrder(String value) {
        return value == null || value.isBlank() ? 0 : Integer.parseInt(value);
    }

    private static Instant parseInstant(String value) {
        return value == null || value.isBlank() ? Instant.EPOCH : Instant.parse(value);
    }
}
