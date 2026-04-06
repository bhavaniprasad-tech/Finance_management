package com.bhavaniprasad.moneymanager.controller;

import com.bhavaniprasad.moneymanager.dto.ExpenseDTO;
import com.bhavaniprasad.moneymanager.service.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.nio.charset.StandardCharsets;


import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    private ResponseEntity<ExpenseDTO> addExpense(@Valid @RequestBody ExpenseDTO dto) {
        ExpenseDTO saved = expenseService.addExpense(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ExpenseDTO>> getExpenses() {
        List<ExpenseDTO> expenses = expenseService.getCurrentMonthExpensesForCurrentUser();
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ExpenseDTO>> getExpensesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(expenseService.getExpensesPage(page, size, keyword, sortField, sortDirection));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteExpense(@PathVariable Long id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseDTO> getExpenseById(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.getExpenseById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ExpenseDTO> updateExpense(@PathVariable Long id, @Valid @RequestBody ExpenseDTO dto) {
        return ResponseEntity.ok(expenseService.updateExpense(id, dto));
    }

    private byte[] convertToCSV(List<ExpenseDTO> expenses) {
        StringBuilder sb = new StringBuilder("Name,Amount,Date,Category\n");

        for (ExpenseDTO e : expenses) {
            sb.append(e.getName()).append(",")
                    .append(e.getAmount()).append(",")
                    .append(e.getDate()).append(",")
                    .append(e.getCategoryName()).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadExpenseCSV() {
        List<ExpenseDTO> expenses = expenseService.getAllExpensesForExport();
        byte[] csv = convertToCSV(expenses);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=expenses.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }

    @PostMapping("/email")
    public ResponseEntity<String> emailExpenseCSV() {
        List<ExpenseDTO> expenses = expenseService.getAllExpensesForExport();
        byte[] csv = convertToCSV(expenses);

        expenseService.emailExpenseCSV(csv);

        return ResponseEntity.ok("Expense report emailed successfully");
    }



}
