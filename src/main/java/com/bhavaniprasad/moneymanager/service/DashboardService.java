package com.bhavaniprasad.moneymanager.service;

import com.bhavaniprasad.moneymanager.dto.CategorySummaryDTO;
import com.bhavaniprasad.moneymanager.dto.DashboardSummaryDTO;
import com.bhavaniprasad.moneymanager.dto.ExpenseDTO;
import com.bhavaniprasad.moneymanager.dto.IncomeDTO;
import com.bhavaniprasad.moneymanager.dto.MonthlyTrendDTO;
import com.bhavaniprasad.moneymanager.dto.RecentTransactionDTO;
import com.bhavaniprasad.moneymanager.entity.ProfileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Stream.concat;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private static final LocalDate ANALYTICS_START_DATE = LocalDate.of(1970, 1, 1);

    private final IncomeService incomeService;
    private final ExpenseService expenseService;
    private final CategoryService categoryService;
    private final ProfileService profileService;

    public DashboardSummaryDTO getDashboard() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeDTO> latestIncomes = incomeService.getLatest5IncomesForCurrentUser();
        List<ExpenseDTO> latestExpenses = expenseService.getLatest5ExpensesForCurrentUser();
        List<RecentTransactionDTO> recentTransactions =
                concat(
                        latestIncomes.stream().map(income ->
                                RecentTransactionDTO.builder()
                                        .id(income.getId())
                                        .profileId(profile.getId())
                                        .icon(income.getIcon())
                                        .name(income.getName())
                                        .amount(income.getAmount())
                                        .date(income.getDate())
                                        .createdAt(income.getCreatedAt())
                                        .updatedAt(income.getUpdatedAt())
                                        .type("income")
                                        .build()
                        ),
                        latestExpenses.stream().map(expense ->
                                RecentTransactionDTO.builder()
                                        .id(expense.getId())
                                        .profileId(profile.getId())
                                        .icon(expense.getIcon())
                                        .name(expense.getName())
                                        .amount(expense.getAmount())
                                        .date(expense.getDate())
                                        .createdAt(expense.getCreatedAt())
                                        .updatedAt(expense.getUpdatedAt())
                                        .type("expense")
                                        .build()
                        )
                ).sorted((a, b) -> {
                    int cmp = b.getDate().compareTo(a.getDate());
                    if (cmp == 0 && a.getCreatedAt() != null && b.getCreatedAt() != null) {
                        return b.getCreatedAt().compareTo(a.getCreatedAt());
                    }
                    return cmp;
                }).limit(10).collect(Collectors.toList());

        BigDecimal totalIncome = incomeService.getTotalIncomeForCurrentUser();
        BigDecimal totalExpense = expenseService.getTotalExpenseForCurrentUser();

        return DashboardSummaryDTO.builder()
                .totalBalance(totalIncome.subtract(totalExpense))
                .totalIncome(totalIncome)
                .totalExpense(totalExpense)
                .recent5Expenses(latestExpenses)
                .recent5Incomes(latestIncomes)
                .recentTransactions(recentTransactions)
                .categoryTotals(buildCategoryTotals())
                .monthlyTrends(buildMonthlyTrends(6))
                .build();
    }

    private List<CategorySummaryDTO> buildCategoryTotals() {
        List<CategorySummaryDTO> summaries = new ArrayList<>();
        categoryService.getCategoriesForCurrentUser().forEach(category -> {
            BigDecimal total = "income".equalsIgnoreCase(category.getType())
                    ? incomeService.filterIncomes(ANALYTICS_START_DATE, LocalDate.now(), "", category.getId(), org.springframework.data.domain.Sort.unsorted())
                        .stream().map(IncomeDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add)
                    : expenseService.filterExpenses(ANALYTICS_START_DATE, LocalDate.now(), "", category.getId(), org.springframework.data.domain.Sort.unsorted())
                        .stream().map(ExpenseDTO::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
            summaries.add(CategorySummaryDTO.builder()
                    .category(category.getName())
                    .type(category.getType())
                    .total(total)
                    .build());
        });
        return summaries;
    }

    private List<MonthlyTrendDTO> buildMonthlyTrends(int monthsBack) {
        List<MonthlyTrendDTO> trends = new ArrayList<>();
        YearMonth current = YearMonth.now();
        for (int i = monthsBack - 1; i >= 0; i--) {
            YearMonth month = current.minusMonths(i);
            LocalDate start = month.atDay(1);
            LocalDate end = month.atEndOfMonth();
            BigDecimal income = incomeService.getTotalIncomeForCurrentUserBetween(start, end);
            BigDecimal expense = expenseService.getTotalExpenseForCurrentUserBetween(start, end);
            trends.add(MonthlyTrendDTO.builder()
                    .month(month.toString())
                    .income(income)
                    .expense(expense)
                    .net(income.subtract(expense))
                    .build());
        }
        return trends;
    }
}
