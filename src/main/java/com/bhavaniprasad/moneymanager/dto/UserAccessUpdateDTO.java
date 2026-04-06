package com.bhavaniprasad.moneymanager.dto;

import com.bhavaniprasad.moneymanager.entity.UserRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserAccessUpdateDTO {

    @NotNull(message = "Role is required")
    private UserRole role;

    @NotNull(message = "isActive is required")
    private Boolean isActive;
}

