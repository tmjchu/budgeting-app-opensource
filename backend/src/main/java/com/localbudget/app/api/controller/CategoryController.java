package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.response.CategoryResponse;
import com.localbudget.app.domain.model.CategoryDO;
import com.localbudget.app.domain.service.CategoryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponse> getCategories() {
        return categoryService.findAll().stream().map(this::toResponse).toList();
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
