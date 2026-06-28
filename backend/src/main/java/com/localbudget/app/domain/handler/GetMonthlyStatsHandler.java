package com.localbudget.app.domain.handler;

import com.localbudget.app.domain.model.MonthlyStats;
import com.localbudget.app.domain.service.StatsAggregationService;
import com.localbudget.app.domain.service.TransactionQueryService;
import java.time.YearMonth;
import org.springframework.stereotype.Component;

@Component
public class GetMonthlyStatsHandler {

    private final TransactionQueryService transactionQueryService;
    private final StatsAggregationService statsAggregationService;

    public GetMonthlyStatsHandler(
            TransactionQueryService transactionQueryService,
            StatsAggregationService statsAggregationService) {
        this.transactionQueryService = transactionQueryService;
        this.statsAggregationService = statsAggregationService;
    }

    public MonthlyStats handle(YearMonth month) {
        YearMonth resolvedMonth = month == null ? YearMonth.now() : month;
        return statsAggregationService.buildMonthlyStats(resolvedMonth, transactionQueryService.findAll());
    }
}
