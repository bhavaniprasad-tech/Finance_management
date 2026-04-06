package com.bhavaniprasad.moneymanager.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ExpenseDTO {

    private Long id;
    @NotBlank(message = "Name is required")
    private String name;
    private String icon;
    private String categoryName;
    @NotNull(message = "Category is required")
    private Long categoryId;
    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", message = "Amount must be greater than 0")
    private BigDecimal amount;
    @NotNull(message = "Date is required")
    private LocalDate date;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
