package com.bhavaniprasad.moneymanager.service;

import com.bhavaniprasad.moneymanager.dto.IncomeDTO;
import com.bhavaniprasad.moneymanager.entity.CategoryEntity;
import com.bhavaniprasad.moneymanager.entity.IncomeEntity;
import com.bhavaniprasad.moneymanager.entity.ProfileEntity;
import com.bhavaniprasad.moneymanager.repository.CategoryRepository;
import com.bhavaniprasad.moneymanager.repository.IncomeRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IncomeServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private IncomeRepository incomeRepository;
    @Mock
    private ProfileService profileService;
    @Mock
    private BrevoEmailService brevoEmailService;

    @InjectMocks
    private IncomeService incomeService;

    @Test
    void deleteIncome_shouldSoftDeleteRecord() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        IncomeEntity income = IncomeEntity.builder().id(10L).profile(profile).build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(incomeRepository.findByIdAndProfileIdAndDeletedAtIsNull(10L, 1L)).thenReturn(Optional.of(income));

        incomeService.deleteIncome(10L);

        ArgumentCaptor<IncomeEntity> captor = ArgumentCaptor.forClass(IncomeEntity.class);
        verify(incomeRepository).save(captor.capture());
        verify(incomeRepository, never()).delete(any());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    void getIncomesPage_shouldApplySearchAndPagination() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        CategoryEntity category = CategoryEntity.builder().id(5L).name("Salary").build();
        IncomeEntity income = IncomeEntity.builder()
                .id(7L)
                .name("April Salary")
                .amount(new BigDecimal("1000.00"))
                .date(LocalDate.now())
                .category(category)
                .profile(profile)
                .build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(incomeRepository.findByProfileIdAndDeletedAtIsNullAndNameContainingIgnoreCase(
                eq(1L), eq("salary"), any(PageRequest.class)
        )).thenReturn(new PageImpl<>(List.of(income)));

        var page = incomeService.getIncomesPage(0, 10, "salary", "date", "desc");

        assertThat(page.getTotalElements()).isEqualTo(1);
        IncomeDTO dto = page.getContent().getFirst();
        assertThat(dto.getName()).isEqualTo("April Salary");
    }
}

