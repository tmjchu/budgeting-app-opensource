package com.localbudget.app.domain.service;

import com.localbudget.app.domain.model.CategoryStats;
import com.localbudget.app.domain.model.MonthlyStats;
import com.localbudget.app.domain.model.Transaction;
import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class StatsAggregationService {

    public MonthlyStats buildMonthlyStats(YearMonth month, List<Transaction> transactions) {
        List<Transaction> included = transactions.stream()
                .filter(transaction -> !transaction.excluded())
                .filter(transaction -> YearMonth.from(transaction.date()).equals(month))
                .toList();
        BigDecimal income = included.stream()
                .map(Transaction::amount)
                .filter(amount -> amount.signum() < 0)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal spending = included.stream()
                .map(Transaction::amount)
                .filter(amount -> amount.signum() > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        return new MonthlyStats(
                month,
                income,
                spending,
                income.subtract(spending),
                included.size());
    }

    public List<CategoryStats> buildCategoryStats(YearMonth month, List<Transaction> transactions) {
        Map<String, List<Transaction>> byCategory = transactions.stream()
                .filter(transaction -> !transaction.excluded())
                .filter(transaction -> transaction.amount().signum() > 0)
                .filter(transaction -> YearMonth.from(transaction.date()).equals(month))
                .collect(Collectors.groupingBy(Transaction::effectiveCategory));

        return byCategory.entrySet().stream()
                .map(entry -> new CategoryStats(
                        entry.getKey(),
                        entry.getValue().stream()
                                .map(Transaction::amount)
                                .reduce(BigDecimal.ZERO, BigDecimal::add),
                        entry.getValue().size()))
                .sorted(Comparator.comparing(CategoryStats::amount).reversed())
                .toList();
    }
}
