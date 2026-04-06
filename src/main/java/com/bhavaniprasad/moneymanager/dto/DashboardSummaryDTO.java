package com.bhavaniprasad.moneymanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DashboardSummaryDTO {

    private BigDecimal totalBalance;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private List<ExpenseDTO> recent5Expenses;
    private List<IncomeDTO> recent5Incomes;
    private List<RecentTransactionDTO> recentTransactions;
    private List<CategorySummaryDTO> categoryTotals;
    private List<MonthlyTrendDTO> monthlyTrends;
}

