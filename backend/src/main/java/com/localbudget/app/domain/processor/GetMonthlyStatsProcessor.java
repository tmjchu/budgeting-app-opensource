package com.localbudget.app.domain.processor;

import com.localbudget.app.domain.model.MonthlyStats;
import com.localbudget.app.domain.service.StatsAggregationService;
import com.localbudget.app.domain.service.TransactionService;
import java.time.YearMonth;
import org.springframework.stereotype.Component;

@Component
public class GetMonthlyStatsProcessor {

    private final TransactionService transactionService;
    private final StatsAggregationService statsAggregationService;

    public GetMonthlyStatsProcessor(
            TransactionService transactionService,
            StatsAggregationService statsAggregationService) {
        this.transactionService = transactionService;
        this.statsAggregationService = statsAggregationService;
    }

    public MonthlyStats handle(YearMonth month) {
        YearMonth resolvedMonth = month == null ? YearMonth.now() : month;
        return statsAggregationService.buildMonthlyStats(
                resolvedMonth, transactionService.findAll());
    }
}
