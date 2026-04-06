package com.bhavaniprasad.moneymanager.service;

import com.bhavaniprasad.moneymanager.dto.CategoryDTO;
import com.bhavaniprasad.moneymanager.entity.CategoryEntity;
import com.bhavaniprasad.moneymanager.entity.ProfileEntity;
import com.bhavaniprasad.moneymanager.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class CategoryService {

    private static final Set<String> ALLOWED_TYPES = Set.of("income", "expense");

    private final ProfileService profileService;
    private final CategoryRepository categoryRepository;

    //save category
    public CategoryDTO saveCategory(CategoryDTO categoryDTO) {
        ProfileEntity profile = profileService.getCurrentProfile();
        String safeName = normalizeName(categoryDTO.getName());
        String safeType = normalizeType(categoryDTO.getType());

        if (categoryRepository.existsByNameIgnoreCaseAndProfileId(safeName, profile.getId())) {
            log.warn("Category create blocked - duplicate name='{}' for profileId={}", safeName, profile.getId());
            throw new ResponseStatusException(CONFLICT, "Category with this name already exists");
        }

        CategoryEntity newCategory = toEntity(categoryDTO, profile, safeName, safeType);
        newCategory = categoryRepository.save(newCategory);
        log.info("Category created id={} name='{}' type='{}' profileId={}", newCategory.getId(), newCategory.getName(), newCategory.getType(), profile.getId());
        return toDTO(newCategory);
    }

    //get categories for current user
    public List<CategoryDTO> getCategoriesForCurrentUser() {
        ProfileEntity profile = profileService.getCurrentProfile();
        List<CategoryEntity> categories = categoryRepository.findByProfileId(profile.getId());
        return categories.stream().map(this::toDTO).toList();
    }

    //get categories by type for current user
    public List<CategoryDTO> getCategoriesByTypeForCurrentUser(String type) {
        ProfileEntity profile = profileService.getCurrentProfile();
        String safeType = normalizeType(type);
        List<CategoryEntity> entities = categoryRepository.findByTypeAndProfileId(safeType, profile.getId());
        return entities.stream().map(this::toDTO).toList();
    }

    public CategoryDTO updateCategory(Long categoryId, CategoryDTO dto) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity existingCategory = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));

        String safeName = normalizeName(dto.getName());
        String safeType = normalizeType(dto.getType());

        if (Boolean.TRUE.equals(categoryRepository.existsByNameIgnoreCaseAndProfileIdAndIdNot(safeName, profile.getId(), categoryId))) {
            throw new ResponseStatusException(CONFLICT, "Category with this name already exists");
        }

        existingCategory.setName(safeName);
        existingCategory.setIcon(dto.getIcon());
        existingCategory.setType(safeType);
        existingCategory = categoryRepository.save(existingCategory);
        log.info("Category updated id={} name='{}' type='{}' profileId={}", existingCategory.getId(), existingCategory.getName(), existingCategory.getType(), profile.getId());
        return toDTO(existingCategory);
    }

    public void deleteCategory(Long categoryId) {
        ProfileEntity profile = profileService.getCurrentProfile();
        CategoryEntity existingCategory = categoryRepository.findByIdAndProfileId(categoryId, profile.getId())
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Category not found"));
        categoryRepository.delete(existingCategory);
        log.info("Category deleted id={} profileId={}", categoryId, profile.getId());
    }

    //helper methods
    private CategoryEntity toEntity(CategoryDTO categoryDTO, ProfileEntity profile, String safeName, String safeType) {
        return CategoryEntity.builder()
                .name(safeName)
                .icon(categoryDTO.getIcon())
                .profile(profile)
                .type(safeType)
                .build();
    }

    private CategoryDTO toDTO(CategoryEntity entity) {
        return CategoryDTO.builder()
                .id(entity.getId())
                .profileId(entity.getProfile() != null ? entity.getProfile().getId() : null)
                .name(entity.getName())
                .icon(entity.getIcon())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .type(entity.getType())
                .build();
    }

    private String normalizeName(String name) {
        String safeName = name == null ? "" : name.trim();
        if (safeName.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Category name is required");
        }
        return safeName;
    }

    private String normalizeType(String type) {
        String safeType = type == null ? "" : type.trim().toLowerCase();
        if (!ALLOWED_TYPES.contains(safeType)) {
            throw new ResponseStatusException(BAD_REQUEST, "Category type must be either 'income' or 'expense'");
        }
        return safeType;
    }
}
