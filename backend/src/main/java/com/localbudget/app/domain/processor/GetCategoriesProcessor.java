package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.model.CategoryDO;
import com.localbudget.app.domain.service.CategoryService;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GetCategoriesProcessor {

    private final CategoryService categoryService;

    public GetCategoriesProcessor(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    public List<CategoryDO> handle() {
        return categoryService.findAll();
    }
}
