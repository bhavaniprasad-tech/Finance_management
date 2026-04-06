package com.bhavaniprasad.moneymanager.service;

import com.bhavaniprasad.moneymanager.dto.IncomeDTO;
import com.bhavaniprasad.moneymanager.entity.CategoryEntity;
import com.bhavaniprasad.moneymanager.entity.IncomeEntity;
import com.bhavaniprasad.moneymanager.entity.ProfileEntity;
import com.bhavaniprasad.moneymanager.repository.CategoryRepository;
import com.bhavaniprasad.moneymanager.repository.IncomeRepository;
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
public class IncomeService {


    private final CategoryRepository categoryRepository;
    private final IncomeRepository incomeRepository;
    private final ProfileService profileService;
    private final BrevoEmailService brevoEmailService;
    private static final Set<String> ALLOWED_PAGE_SORT_FIELDS = Set.of("date", "amount", "name", "createdAt");


    //adds new expense to the database
    public IncomeDTO addIncome(IncomeDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity category = categoryRepository.findByIdAndProfileId(dto.getCategoryId(), profile.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));
        IncomeEntity income = toEntity(dto, profile, category);
        income = incomeRepository.save(income);
        return toDTO(income);
    }

    //Retrieves all expenses for current month/based on the start date and end date
    public List<IncomeDTO> getCurrentMonthIncomesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        LocalDate now = LocalDate.now();
        LocalDate startDate = now.withDayOfMonth(1);
        LocalDate endDate = now.withDayOfMonth(now.lengthOfMonth());
        List<IncomeEntity> list = incomeRepository.findByProfileIdAndDeletedAtIsNullAndDateBetween(profile.getId(), startDate, endDate);
        return list.stream().map(this::toDTO).toList();
    }

    //delete expense by id for current user
    public void deleteIncome(Long incomeId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity entity = incomeRepository.findByIdAndProfileIdAndDeletedAtIsNull(incomeId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Income not found"));
        entity.setDeletedAt(LocalDateTime.now());
        incomeRepository.save(entity);
    }

    public IncomeDTO getIncomeById(Long incomeId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity entity = incomeRepository.findByIdAndProfileIdAndDeletedAtIsNull(incomeId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Income not found"));
        return toDTO(entity);
    }

    public IncomeDTO updateIncome(Long incomeId, IncomeDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        IncomeEntity existing = incomeRepository.findByIdAndProfileIdAndDeletedAtIsNull(incomeId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Income not found"));

        CategoryEntity category = categoryRepository.findByIdAndProfileId(dto.getCategoryId(), profile.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));

        existing.setName(dto.getName());
        existing.setIcon(dto.getIcon());
        existing.setAmount(dto.getAmount());
        existing.setDate(dto.getDate());
        existing.setCategory(category);
        return toDTO(incomeRepository.save(existing));
    }

    //get latest 5 incomes for the current user
    public List<IncomeDTO> getLatest5IncomesForCurrentUser(){
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepository.findTop5ByProfileIdAndDeletedAtIsNullOrderByDateDesc(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

    //get total incomes of the current user
    public BigDecimal getTotalIncomeForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        BigDecimal total = incomeRepository.findTotalIncomeByProfileId(profile.getId());
        return total != null ? total : BigDecimal.ZERO;
    }

    //filter incomes
    public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Sort sort) {
        return filterIncomes(startDate, endDate, keyword, null, sort);
    }

    public List<IncomeDTO> filterIncomes(LocalDate startDate, LocalDate endDate, String keyword, Long categoryId, Sort sort) {
        if (startDate.isAfter(endDate)) {
            throw new ResponseStatusException(BAD_REQUEST, "Start date cannot be after end date");
        }
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list = incomeRepository.filterIncomes(profile.getId(), startDate, endDate, keyword, categoryId, sort);
        return list.stream().map(this::toDTO).toList();
    }

    public BigDecimal getTotalIncomeForCurrentUserBetween(LocalDate startDate, LocalDate endDate) {
        ProfileEntity profile = profileService.getCurrentProfile();
        return incomeRepository.findTotalIncomeByProfileIdAndDateBetween(profile.getId(), startDate, endDate);
    }

    public Page<IncomeDTO> getIncomesPage(int page, int size, String keyword, String sortField, String sortDirection) {
        ProfileEntity profile = profileService.getCurrentProfile();
        String safeKeyword = keyword == null ? "" : keyword.trim();
        Sort.Direction direction = "desc".equalsIgnoreCase(sortDirection) ? Sort.Direction.DESC : Sort.Direction.ASC;
        String safeSortField = sortField == null || sortField.isBlank() ? "date" : sortField;
        if (!ALLOWED_PAGE_SORT_FIELDS.contains(safeSortField)) {
            throw new ResponseStatusException(BAD_REQUEST, "Invalid sort field");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, safeSortField));
        return incomeRepository
                .findByProfileIdAndDeletedAtIsNullAndNameContainingIgnoreCase(profile.getId(), safeKeyword, pageable)
                .map(this::toDTO);
    }



    //helper methods
    private IncomeEntity toEntity(IncomeDTO dto, ProfileEntity profile, CategoryEntity category) {
        return IncomeEntity.builder()
                .name(dto.getName())
                .icon(dto.getIcon())
                .amount(dto.getAmount())
                .date(dto.getDate())
                .profile(profile)
                .category(category)
                .build();
    }

    private IncomeDTO toDTO(IncomeEntity entity) {
        return IncomeDTO.builder()
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

    public List<IncomeDTO> getAllIncomesForExport() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<IncomeEntity> list =
                incomeRepository.findByProfileIdAndDeletedAtIsNull(profile.getId());
        return list.stream().map(this::toDTO).toList();
    }

    public void emailIncomeCSV(byte[] csvBytes) {
        ProfileEntity profile = profileService.getCurrentProfile();

        brevoEmailService.sendEmailWithAttachment(
                profile.getEmail(),
                "Your Income Report",
                "<p>Please find your <b>income report</b> attached.</p>",
                csvBytes,
                "income.csv"
        );
    }


}
