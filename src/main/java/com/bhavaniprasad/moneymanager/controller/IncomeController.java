package com.bhavaniprasad.moneymanager.controller;

import com.bhavaniprasad.moneymanager.dto.IncomeDTO;
import com.bhavaniprasad.moneymanager.service.IncomeService;
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
@RequestMapping("/incomes")
public class IncomeController {

    private final IncomeService incomeService;

    @PostMapping
    private ResponseEntity<IncomeDTO> addExpense(@Valid @RequestBody IncomeDTO dto) {
        IncomeDTO saved = incomeService.addIncome(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<IncomeDTO>> getIncomes() {
        List<IncomeDTO> incomes = incomeService.getCurrentMonthIncomesForCurrentUser();
        return ResponseEntity.ok(incomes);
    }

    @GetMapping("/search")
    public ResponseEntity<Page<IncomeDTO>> getIncomesPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "") String keyword,
            @RequestParam(defaultValue = "date") String sortField,
            @RequestParam(defaultValue = "desc") String sortDirection
    ) {
        return ResponseEntity.ok(incomeService.getIncomesPage(page, size, keyword, sortField, sortDirection));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteIncome(@PathVariable Long id) {
        incomeService.deleteIncome(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<IncomeDTO> getIncomeById(@PathVariable Long id) {
        return ResponseEntity.ok(incomeService.getIncomeById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IncomeDTO> updateIncome(@PathVariable Long id, @Valid @RequestBody IncomeDTO dto) {
        return ResponseEntity.ok(incomeService.updateIncome(id, dto));
    }

    private byte[] convertToCSV(List<IncomeDTO> incomes) {
        StringBuilder sb = new StringBuilder("Name,Amount,Date,Category\n");

        for (IncomeDTO i : incomes) {
            sb.append(i.getName()).append(",")
                    .append(i.getAmount()).append(",")
                    .append(i.getDate()).append(",")
                    .append(i.getCategoryName()).append("\n");
        }
        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadIncomeCSV() {
        List<IncomeDTO> incomes = incomeService.getAllIncomesForExport();
        byte[] csv = convertToCSV(incomes);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=incomes.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .body(csv);
    }

    @PostMapping("/email")
    public ResponseEntity<String> emailIncomeCSV() {
        List<IncomeDTO> incomes = incomeService.getAllIncomesForExport();
        byte[] csv = convertToCSV(incomes);

        incomeService.emailIncomeCSV(csv);

        return ResponseEntity.ok("Income report emailed successfully");
    }



}
