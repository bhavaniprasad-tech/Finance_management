package com.bhavaniprasad.moneymanager.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CategoryDTO {

    private Long id;
    private Long profileId;
    @NotBlank(message = "Category name is required")
    private String name;
    private String icon;
    @NotBlank(message = "Category type is required")
    private String type;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
