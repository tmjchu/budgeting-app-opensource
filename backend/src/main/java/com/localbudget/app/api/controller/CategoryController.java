package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.response.CategoryResponse;
import com.localbudget.app.domain.model.CategoryDO;
import com.localbudget.app.domain.processor.GetCategoriesProcessor;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final GetCategoriesProcessor getCategoriesProcessor;

    @GetMapping
    public List<CategoryResponse> getCategories() {
        log.info("Categories API Invoked");
        List<CategoryResponse> response =
                getCategoriesProcessor.process().stream().map(this::toResponse).toList();
        log.info("Categories API Completed");
        return response;
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
