package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.model.CategoryStats;
import com.localbudget.app.domain.service.CategoryService;
import com.localbudget.app.domain.service.StatsAggregationService;
import com.localbudget.app.domain.service.TransactionService;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GetCategoryStatsProcessor {

    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final StatsAggregationService statsAggregationService;

    public List<CategoryStats> process(YearMonth month) {
        YearMonth resolvedMonth = month == null ? YearMonth.now() : month;
        return statsAggregationService.buildCategoryStats(
                resolvedMonth, transactionService.findAll(), categoryService.displayNamesById());
    }
}
