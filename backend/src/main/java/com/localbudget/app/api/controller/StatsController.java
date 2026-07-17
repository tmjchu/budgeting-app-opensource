package com.localbudget.app.api.controller;

import com.localbudget.app.api.model.response.CategoryStatsResponse;
import com.localbudget.app.api.model.response.MonthlyStatsResponse;
import com.localbudget.app.domain.model.CategoryStats;
import com.localbudget.app.domain.model.MonthlyStats;
import com.localbudget.app.domain.processor.GetCategoryStatsProcessor;
import com.localbudget.app.domain.processor.GetMonthlyStatsProcessor;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class StatsController {

    private final GetMonthlyStatsProcessor getMonthlyStatsProcessor;
    private final GetCategoryStatsProcessor getCategoryStatsProcessor;

    @GetMapping("/monthly")
    public MonthlyStatsResponse getMonthlyStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        log.info("Monthly Stats API Invoked");
        MonthlyStats stats = getMonthlyStatsProcessor.process(month);
        MonthlyStatsResponse response =
                new MonthlyStatsResponse(
                        stats.month().toString(),
                        stats.income(),
                        stats.spending(),
                        stats.netCashFlow(),
                        stats.transactionCount());
        log.info("Monthly Stats API Completed");
        return response;
    }

    @GetMapping("/categories")
    public List<CategoryStatsResponse> getCategoryStats(
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth month) {
        log.info("Category Stats API Invoked");
        List<CategoryStatsResponse> response =
                getCategoryStatsProcessor.process(month).stream().map(this::toResponse).toList();
        log.info("Category Stats API Completed");
        return response;
    }

    private CategoryStatsResponse toResponse(CategoryStats stats) {
        return new CategoryStatsResponse(
                stats.category(), stats.amount(), stats.transactionCount());
    }
}
