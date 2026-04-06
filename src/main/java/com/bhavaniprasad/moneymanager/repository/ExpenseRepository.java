package com.bhavaniprasad.moneymanager.repository;

import com.bhavaniprasad.moneymanager.entity.ExpenseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {

    //select * from tb_expenses where profileId = ? orderby date desc
    List<ExpenseEntity> findByProfileIdAndDeletedAtIsNullOrderByDateDesc(Long profileId);

    //select * from tbl_expenses where profile_id = ? order by date desc limit 5
    List<ExpenseEntity>findTop5ByProfileIdAndDeletedAtIsNullOrderByDateDesc(Long profileId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ExpenseEntity e WHERE e.profile.id = :profileId AND e.deletedAt IS NULL")
    BigDecimal findTotalExpenseByProfileId(@Param("profileId") Long profileId);

    //select * from tbl_expenses where profileId = ?1 and date bewteen ?2 and ?3 and name like %?4%
    @Query("SELECT e FROM ExpenseEntity e WHERE e.profile.id = :profileId " +
            "AND e.deletedAt IS NULL " +
            "AND e.date BETWEEN :startDate AND :endDate " +
            "AND (:keyword = '' OR LOWER(e.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR e.category.id = :categoryId)")
    List<ExpenseEntity> filterExpenses(
            @Param("profileId") Long profileId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Sort sort
    );

    //select * from tbl_expenses where profileId = ?1 and date bewteen ?2 and ?3
    List<ExpenseEntity> findByProfileIdAndDeletedAtIsNullAndDateBetween(Long profileId, LocalDate startDate, LocalDate endDate);

    //select * from tbl_expenses where profile_id = ?1 and date = ?2
    List<ExpenseEntity> findByProfileIdAndDeletedAtIsNullAndDate(Long profileId, LocalDate date);

    List<ExpenseEntity> findByProfileIdAndDeletedAtIsNull(Long profileId);

    Page<ExpenseEntity> findByProfileIdAndDeletedAtIsNullAndNameContainingIgnoreCase(
            Long profileId,
            String keyword,
            Pageable pageable
    );

    Optional<ExpenseEntity> findByIdAndProfileIdAndDeletedAtIsNull(Long id, Long profileId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM ExpenseEntity e WHERE e.profile.id = :profileId AND e.deletedAt IS NULL AND e.date BETWEEN :startDate AND :endDate")
    BigDecimal findTotalExpenseByProfileIdAndDateBetween(
            @Param("profileId") Long profileId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );
}
