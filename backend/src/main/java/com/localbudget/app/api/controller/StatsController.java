package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.response.CategoryStatsResponse;
import com.localbudget.app.api.model.response.MonthlyStatsResponse;
import com.localbudget.app.domain.handler.GetCategoryStatsHandler;
import com.localbudget.app.domain.handler.GetMonthlyStatsHandler;
import com.localbudget.app.domain.model.CategoryStats;
import com.localbudget.app.domain.model.MonthlyStats;
import java.time.YearMonth;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
public class StatsController {

    private final GetMonthlyStatsHandler getMonthlyStatsHandler;
    private final GetCategoryStatsHandler getCategoryStatsHandler;

    public StatsController(
            GetMonthlyStatsHandler getMonthlyStatsHandler,
            GetCategoryStatsHandler getCategoryStatsHandler) {
        this.getMonthlyStatsHandler = getMonthlyStatsHandler;
        this.getCategoryStatsHandler = getCategoryStatsHandler;
    }

    @GetMapping("/monthly")
    public MonthlyStatsResponse getMonthlyStats(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        MonthlyStats stats = getMonthlyStatsHandler.handle(month);
        return new MonthlyStatsResponse(
                stats.month().toString(),
                stats.income(),
                stats.spending(),
                stats.netCashFlow(),
                stats.transactionCount());
    }

    @GetMapping("/categories")
    public List<CategoryStatsResponse> getCategoryStats(
            @RequestParam(required = false)
            @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        return getCategoryStatsHandler.handle(month).stream()
                .map(this::toResponse)
                .toList();
    }

    private CategoryStatsResponse toResponse(CategoryStats stats) {
        return new CategoryStatsResponse(stats.category(), stats.amount(), stats.transactionCount());
    }
}
