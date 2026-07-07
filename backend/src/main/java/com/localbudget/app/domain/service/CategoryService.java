package com.localbudget.app.domain.service;

import com.localbudget.app.converter.CategoryConverter;
import com.localbudget.app.data.repository.CategoryCsvRepository;
import com.localbudget.app.domain.model.CategoryDO;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private static final String UNCATEGORIZED_ID = "uncategorized";

    private final CategoryCsvRepository categoryRepository;
    private final CategoryConverter categoryConverter;

    public List<CategoryDO> findAll() {
        return categoryRepository.findAll().stream()
                .map(categoryConverter::fromCsv)
                .sorted(Comparator.comparing(CategoryDO::sortOrder))
                .toList();
    }

    public Optional<CategoryDO> findActiveById(String categoryId) {
        if (categoryId == null || categoryId.isBlank()) {
            return Optional.empty();
        }
        return findById(categoryId).filter(category -> !category.archived());
    }

    public Optional<CategoryDO> findById(String categoryId) {
        return findAll().stream()
                .filter(category -> category.categoryId().equals(categoryId))
                .findFirst();
    }

    public String displayName(String categoryId) {
        return findById(categoryId).map(CategoryDO::displayName).orElse("Uncategorized");
    }

    public String displayCategoryForTransaction(
            String localCategoryId, String legacyLocalCategory, String primaryCategory) {
        if (localCategoryId != null && !localCategoryId.isBlank()) {
            return displayName(localCategoryId);
        }
        if (legacyLocalCategory != null && !legacyLocalCategory.isBlank()) {
            return legacyLocalCategory;
        }
        if (primaryCategory != null && !primaryCategory.isBlank()) {
            return primaryCategory;
        }
        return displayName(UNCATEGORIZED_ID);
    }

    public Map<String, CategoryDO> byId() {
        return findAll().stream()
                .collect(Collectors.toMap(CategoryDO::categoryId, Function.identity()));
    }

    public Map<String, String> displayNamesById() {
        return findAll().stream()
                .collect(Collectors.toMap(CategoryDO::categoryId, CategoryDO::displayName));
    }
}
