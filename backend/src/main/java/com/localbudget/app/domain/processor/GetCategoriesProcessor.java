package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.model.CategoryDO;
import com.localbudget.app.domain.service.CategoryService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCategoriesProcessor {

    private final CategoryService categoryService;

    public List<CategoryDO> handle() {
        return categoryService.findAll();
    }
}
