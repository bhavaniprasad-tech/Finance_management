package com.bhavaniprasad.moneymanager.controller;

import com.bhavaniprasad.moneymanager.dto.ExpenseDTO;
import com.bhavaniprasad.moneymanager.dto.FilterDTO;
import com.bhavaniprasad.moneymanager.dto.IncomeDTO;
import com.bhavaniprasad.moneymanager.service.ExpenseService;
import com.bhavaniprasad.moneymanager.service.IncomeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@RestController
@RequiredArgsConstructor
@RequestMapping("/filter")
public class FilterController {

    private final ExpenseService expenseService;
    private final IncomeService incomeService;
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of("date", "amount", "name", "createdAt");
    private static final LocalDate DEFAULT_START_DATE = LocalDate.of(1970, 1, 1);

    @PostMapping
    public ResponseEntity<?> filterTransactions(@Valid @RequestBody FilterDTO filter){
        //preparing the data or validation
        LocalDate startDate = filter.getStartDate() != null ? filter.getStartDate() : DEFAULT_START_DATE;
        LocalDate endDate = filter.getEndDate() != null ? filter.getEndDate() : LocalDate.now();
        String keyword = filter.getKeyword() != null ? filter.getKeyword() : "";
        String sortField = filter.getSortField() != null ? filter.getSortField() : "date";
        if (!ALLOWED_SORT_FIELDS.contains(sortField)) {
            return ResponseEntity.badRequest().body("Invalid sort field. Allowed values: " + ALLOWED_SORT_FIELDS);
        }
        Sort.Direction direction = "desc".equalsIgnoreCase(filter.getSortOrder()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortField);
        if ("income".equalsIgnoreCase(filter.getType())){
            List<IncomeDTO> incomes = incomeService.filterIncomes(startDate, endDate, keyword, filter.getCategoryId(), sort);
            return ResponseEntity.ok(incomes);
        }else if ("expense".equalsIgnoreCase(filter.getType())){
            List<ExpenseDTO> expenses = expenseService.filterExpenses(startDate, endDate, keyword, filter.getCategoryId(), sort);
            return ResponseEntity.ok(expenses);
        }else {
            return ResponseEntity.badRequest().body("Invalid type. Must be 'income' or 'expense'");
        }
    }
}
