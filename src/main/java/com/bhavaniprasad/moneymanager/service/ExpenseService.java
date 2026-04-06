package com.bhavaniprasad.moneymanager.service;

import com.bhavaniprasad.moneymanager.dto.ExpenseDTO;
import com.bhavaniprasad.moneymanager.entity.CategoryEntity;
import com.bhavaniprasad.moneymanager.entity.ExpenseEntity;
import com.bhavaniprasad.moneymanager.entity.ProfileEntity;
import com.bhavaniprasad.moneymanager.repository.CategoryRepository;
import com.bhavaniprasad.moneymanager.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final CategoryRepository categoryRepository;
    private final ExpenseRepository expenseRepository;
    private final ProfileService profileService;
    private final BrevoEmailService brevoEmailService;
    private static final Set<String> ALLOWED_PAGE_SORT_FIELDS = Set.of("date", "amount", "name", "createdAt");


    //adds new expense to the database
    public ExpenseDTO addExpense(ExpenseDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findByIdAndProfileId(dto.getCategoryId(), profile.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));
        ExpenseEntity newExpense = toEntity(dto, profile, category);
        newExpense = expenseRepository.save(newExpense);
        return toDTO(newExpense);
    }

    //Retrieves all expenses for current month/based on the start date and end date
    public List<ExpenseDTO> getCurrentMonthExpensesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDeletedAtIsNullAndDateBetween(profile.getId(), startDate, endDate);
        return list.stream().map(this::toDTO).toList();
    }

    //delete expense by id for current user
    public void deleteExpense(Long expenseId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity entity = expenseRepository.findByIdAndProfileIdAndDeletedAtIsNull(expenseId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Expense not found"));
        entity.setDeletedAt(LocalDateTime.now());
        expenseRepository.save(entity);
    }

    public ExpenseDTO getExpenseById(Long expenseId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity entity = expenseRepository.findByIdAndProfileIdAndDeletedAtIsNull(expenseId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Expense not found"));
        return toDTO(entity);
    }

    public ExpenseDTO updateExpense(Long expenseId, ExpenseDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        ExpenseEntity existing = expenseRepository.findByIdAndProfileIdAndDeletedAtIsNull(expenseId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Expense not found"));
        CategoryEntity category = categoryRepository.findByIdAndProfileId(dto.getCategoryId(), profile.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));

        existing.setName(dto.getName());
        existing.setIcon(dto.getIcon());
        existing.setAmount(dto.getAmount());
        existing.setDate(dto.getDate());
        existing.setCategory(category);
        return toDTO(expenseRepository.save(existing));
    }

    //get latest 5 expenses for the current user
    public List<ExpenseDTO> getLatest5ExpensesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expenseRepository.findTop5ByProfileIdAndDeletedAtIsNullOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

    //get total expenses of the current user
    public BigDecimal getTotalExpenseForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = expenseRepository.findTotalExpenseByProfileId(profile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    //filter expenses
    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        return filterExpenses(startDate, endDate, keyword, null, sort);
    }

    public List<ExpenseDTO> filterExpenses(LocalDate startDate, LocalDate endDate, String keyword, Long categoryId, Sort sort) {
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Start date cannot be after end date");
        }
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list = expenseRepository.filterExpenses(profile.getId(), startDate, endDate, keyword, categoryId, sort);
        return list.stream().map(this::toDTO).toList();
    }

    public BigDecimal getTotalExpenseForCurrentUserBetween(LocalDate startDate, LocalDate endDate) {
        ProfileEntity profile = profileService.getCurrentProfile();
        return expenseRepository.findTotalExpenseByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
    }

    public Page<ExpenseDTO> getExpensesPage(int page, int size, String keyword, String sortField, String sortDirection) {
        ProfileEntity profile = profileService.getCurrentProfile();
        String safeKeyword = keyword == null ? "" : keyword.trim();
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String safeSortField = sortField == null || sortField.isBlank() ? "date" : sortField;
        if (!ALLOWED_PAGE_SORT_FIELDS.contains(safeSortField)) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid sort field");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, safeSortField));
        return expenseRepository
                .findByProfileIdAndDeletedAtIsNullAndNameContainingIgnoreCase(profile.getId(), safeKeyword, pageable)
                .map(this::toDTO);
    }

    //Notifications
    public List<ExpenseDTO> getExpensesForUserOnDate(Long profileId, LocalDate date){
        List<ExpenseEntity> list = expenseRepository.findByProfileIdAndDeletedAtIsNullAndDate(profileId, date);
        return list.stream().map(this :: toDTO).toList();
    }

    //helper methods
    private ExpenseEntity toEntity(ExpenseDTO dto, ProfileEntity profile, CategoryEntity category) {
        return ExpenseEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private ExpenseDTO toDTO(ExpenseEntity entity) {
        return ExpenseDTO.builder()
                .id(entity.getId())
                .name(entity.getName())
                .icon(entity.getIcon())
                .categoryId(entity.getCategory() != null ? entity.getCategory().getId():null)
                .categoryName(entity.getCategory() != null ? entity.getCategory().getName() : "N/A")
                .amount(entity.getAmount())
                .date(entity.getDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public List<ExpenseDTO> getAllExpensesForExport() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<ExpenseEntity> list =
                expenseRepository.findByProfileIdAndDeletedAtIsNull(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

    public void emailExpenseCSV(byte[] csvBytes) {
        ProfileEntity profile = profileService.getCurrentProfile();

        brevoEmailService.sendEmailWithAttachment(
                profile.getEmail(),
                "Your Expense Report",
                "<p>Please find your <b>expense report</b> attached.</p>",
                csvBytes,
                "expenses.csv"
        );
    }


}
