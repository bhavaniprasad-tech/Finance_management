package com.bhavaniprasad.moneymanager.repository;

import com.bhavaniprasad.moneymanager.entity.IncomeEntity;
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

public interface IncomeRepository extends JpaRepository<IncomeEntity, Long> {

    //select * from tbl_incomes where profileId = ? orderby date desc
    List<IncomeEntity> findByProfileIdAndDeletedAtIsNullOrderByDateDesc(Long profileId);

    //select * from tbl_incomes where profile_id = ? order by date desc limit 5
    List<IncomeEntity>findTop5ByProfileIdAndDeletedAtIsNullOrderByDateDesc(Long profileId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM IncomeEntity i WHERE i.profile.id = :profileId AND i.deletedAt IS NULL")
    BigDecimal findTotalIncomeByProfileId(@Param("profileId") Long profileId);

    //select * from tbl_incomes where profileId = ?1 and date bewteen ?2 and ?3 and name like %?4%
    @Query("SELECT i FROM IncomeEntity i WHERE i.profile.id = :profileId " +
            "AND i.deletedAt IS NULL " +
            "AND i.date BETWEEN :startDate AND :endDate " +
            "AND (:keyword = '' OR LOWER(i.name) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
            "AND (:categoryId IS NULL OR i.category.id = :categoryId)")
    List<IncomeEntity> filterIncomes(
            @Param("profileId") Long profileId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("keyword") String keyword,
            @Param("categoryId") Long categoryId,
            Sort sort
    );

    //select * from tbl_incomes where profileId = ?1 and date bewteen ?2 and ?3
    List<IncomeEntity> findByProfileIdAndDeletedAtIsNullAndDateBetween(Long profileId, LocalDate startDate, LocalDate endDate);

    List<IncomeEntity> findByProfileIdAndDeletedAtIsNull(Long profileId);

    Page<IncomeEntity> findByProfileIdAndDeletedAtIsNullAndNameContainingIgnoreCase(
            Long profileId,
            String keyword,
            Pageable pageable
    );

    Optional<IncomeEntity> findByIdAndProfileIdAndDeletedAtIsNull(Long id, Long profileId);

    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM IncomeEntity i WHERE i.profile.id = :profileId AND i.deletedAt IS NULL AND i.date BETWEEN :startDate AND :endDate")
    BigDecimal findTotalIncomeByProfileIdAndDateBetween(
            @Param("profileId") Long profileId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

}
