package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.response.CategoryResponse;
import com.localbudget.app.domain.model.CategoryDO;
import com.localbudget.app.domain.processor.GetCategoriesProcessor;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final GetCategoriesProcessor getCategoriesProcessor;

    public CategoryController(GetCategoriesProcessor getCategoriesProcessor) {
        this.getCategoriesProcessor = getCategoriesProcessor;
    }

    @GetMapping
    public List<CategoryResponse> getCategories() {
        return getCategoriesProcessor.handle().stream().map(this::toResponse).toList();
    }

    private CategoryResponse toResponse(CategoryDO category) {
        return new CategoryResponse(
                category.categoryId(),
                category.displayName(),
                category.color(),
                category.parentCategoryId(),
                category.archived(),
                category.sortOrder());
    }
}
