package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.model.CategoryStats;
import com.localbudget.app.domain.service.CategoryService;
import com.localbudget.app.domain.service.StatsAggregationService;
import com.localbudget.app.domain.service.TransactionService;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GetCategoryStatsProcessor {

    private final TransactionService transactionService;
    private final CategoryService categoryService;
    private final StatsAggregationService statsAggregationService;

    public GetCategoryStatsProcessor(
            TransactionService transactionService,
            CategoryService categoryService,
            StatsAggregationService statsAggregationService) {
        this.transactionService = transactionService;
        this.categoryService = categoryService;
        this.statsAggregationService = statsAggregationService;
    }

    public List<CategoryStats> handle(YearMonth month) {
        YearMonth resolvedMonth = month == null ? YearMonth.now() : month;
        return statsAggregationService.buildCategoryStats(
                resolvedMonth, transactionService.findAll(), categoryService.displayNamesById());
    }
}
