package com.bhavaniprasad.moneymanager.service;

import com.bhavaniprasad.moneymanager.dto.ExpenseDTO;
import com.bhavaniprasad.moneymanager.entity.CategoryEntity;
import com.bhavaniprasad.moneymanager.entity.ExpenseEntity;
import com.bhavaniprasad.moneymanager.entity.ProfileEntity;
import com.bhavaniprasad.moneymanager.repository.CategoryRepository;
import com.bhavaniprasad.moneymanager.repository.ExpenseRepository;
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
class ExpenseServiceTest {

    @Mock
    private CategoryRepository categoryRepository;
    @Mock
    private ExpenseRepository expenseRepository;
    @Mock
    private ProfileService profileService;
    @Mock
    private BrevoEmailService brevoEmailService;

    @InjectMocks
    private ExpenseService expenseService;

    @Test
    void deleteExpense_shouldSoftDeleteRecord() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        ExpenseEntity expense = ExpenseEntity.builder().id(10L).profile(profile).build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(expenseRepository.findByIdAndProfileIdAndDeletedAtIsNull(10L, 1L)).thenReturn(Optional.of(expense));

        expenseService.deleteExpense(10L);

        ArgumentCaptor<ExpenseEntity> captor = ArgumentCaptor.forClass(ExpenseEntity.class);
        verify(expenseRepository).save(captor.capture());
        verify(expenseRepository, never()).delete(any());
        assertThat(captor.getValue().getDeletedAt()).isNotNull();
    }

    @Test
    void getExpensesPage_shouldApplySearchAndPagination() {
        ProfileEntity profile = ProfileEntity.builder().id(1L).build();
        CategoryEntity category = CategoryEntity.builder().id(5L).name("Food").build();
        ExpenseEntity expense = ExpenseEntity.builder()
                .id(7L)
                .name("Lunch")
                .amount(new BigDecimal("250.00"))
                .date(LocalDate.now())
                .category(category)
                .profile(profile)
                .build();

        when(profileService.getCurrentProfile()).thenReturn(profile);
        when(expenseRepository.findByProfileIdAndDeletedAtIsNullAndNameContainingIgnoreCase(
                eq(1L), eq("lunch"), any(PageRequest.class)
        )).thenReturn(new PageImpl<>(List.of(expense)));

        var page = expenseService.getExpensesPage(0, 10, "lunch", "date", "desc");

        assertThat(page.getTotalElements()).isEqualTo(1);
        ExpenseDTO dto = page.getContent().getFirst();
        assertThat(dto.getName()).isEqualTo("Lunch");
    }
}

