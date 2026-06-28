package com.localbudget.app.domain.handler;

import com.localbudget.app.domain.model.CategoryStats;
import com.localbudget.app.domain.service.StatsAggregationService;
import com.localbudget.app.domain.service.TransactionQueryService;
import java.time.YearMonth;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class GetCategoryStatsHandler {

    private final TransactionQueryService transactionQueryService;
    private final StatsAggregationService statsAggregationService;

    public GetCategoryStatsHandler(
            TransactionQueryService transactionQueryService,
            StatsAggregationService statsAggregationService) {
        this.transactionQueryService = transactionQueryService;
        this.statsAggregationService = statsAggregationService;
    }

    public List<CategoryStats> handle(YearMonth month) {
        YearMonth resolvedMonth = month == null ? YearMonth.now() : month;
        return statsAggregationService.buildCategoryStats(
                resolvedMonth, transactionQueryService.findAll());
    }
}
