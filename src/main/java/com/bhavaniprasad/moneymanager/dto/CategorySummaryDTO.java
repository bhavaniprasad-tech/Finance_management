package com.bhavaniprasad.moneymanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategorySummaryDTO {

    private String category;
    private String type;
    private BigDecimal total;
}

