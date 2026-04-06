package com.bhavaniprasad.moneymanager.dto;

import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FilterDTO {

    @Pattern(regexp = "^(?i)(income|expense)$", message = "Type must be income or expense")
    private String type;
    private LocalDate startDate;
    private LocalDate endDate;
    private String keyword;
    private Long categoryId;
    private String sortField;
    @Pattern(regexp = "^(?i)(asc|desc)$", message = "Sort order must be asc or desc")
    private String sortOrder;
}
