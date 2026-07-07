package com.localbudget.app.data.repository;

import com.localbudget.app.config.BudgetAppProperties;
import com.localbudget.app.converter.CategoryConverter;
import com.localbudget.app.data.model.CategoryCsvRecord;
import com.localbudget.app.domain.model.CategoryDefaults;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Repository;

@Repository
public class CategoryCsvRepository extends CsvSupport {

    private static final String FILE_NAME = "categories.csv";
    private static final String[] HEADERS = {
        "category_id",
        "display_name",
        "color",
        "parent_category_id",
        "archived",
        "sort_order",
        "created_at",
        "updated_at"
    };

    private final CategoryConverter categoryConverter;

    public CategoryCsvRepository(
            BudgetAppProperties properties, CategoryConverter categoryConverter) {
        super(properties);
        this.categoryConverter = categoryConverter;
    }

    public List<CategoryCsvRecord> findAll() {
        if (!Files.exists(path(FILE_NAME))) {
            writeDefaults();
        }

        List<CategoryCsvRecord> categories =
                readRecords(FILE_NAME, HEADERS).stream()
                        .map(
                                record ->
                                        new CategoryCsvRecord(
                                                value(record, "category_id"),
                                                value(record, "display_name"),
                                                value(record, "color"),
                                                value(record, "parent_category_id"),
                                                value(record, "archived"),
                                                value(record, "sort_order"),
                                                value(record, "created_at"),
                                                value(record, "updated_at")))
                        .toList();
        if (categories.isEmpty()) {
            writeDefaults();
            return findAll();
        }
        return categories;
    }

    public void writeAll(List<CategoryCsvRecord> categories) {
        List<CategoryCsvRecord> sorted =
                categories.stream()
                        .sorted(Comparator.comparing(record -> parseSortOrder(record.sortOrder())))
                        .toList();
        writeRows(
                FILE_NAME,
                HEADERS,
                sorted.stream()
                        .map(
                                record ->
                                        List.of(
                                                value(record.categoryId()),
                                                value(record.displayName()),
                                                value(record.color()),
                                                value(record.parentCategoryId()),
                                                value(record.archived()),
                                                value(record.sortOrder()),
                                                value(record.createdAt()),
                                                value(record.updatedAt())))
                        .toList());
    }

    private void writeDefaults() {
        writeAll(CategoryDefaults.defaults().stream().map(categoryConverter::toCsv).toList());
    }

    private static int parseSortOrder(String value) {
        return value == null || value.isBlank() ? 0 : Integer.parseInt(value);
    }
}
