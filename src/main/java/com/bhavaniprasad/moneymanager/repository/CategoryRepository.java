package com.bhavaniprasad.moneymanager.repository;

import com.bhavaniprasad.moneymanager.entity.CategoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<CategoryEntity, Long> {

    //select * from tbl_categories where profile_id = ?
    List<CategoryEntity> findByProfileId(Long profileId);

    //select * from tbl_categories where id = ? and profile_id = ?
    Optional<CategoryEntity> findByIdAndProfileId(Long id, Long profileId);

    //select * from tbl_categories where type = ? and profile_id = ?
    List<CategoryEntity> findByTypeAndProfileId(String type, Long profileId);

    Boolean existsByNameAndProfileId(String name, Long profileId);

    Boolean existsByNameAndProfileIdAndIdNot(String name, Long profileId, Long id);

    // Case-insensitive duplicate checks for cleaner UX
    Boolean existsByNameIgnoreCaseAndProfileId(String name, Long profileId);

    Boolean existsByNameIgnoreCaseAndProfileIdAndIdNot(String name, Long profileId, Long id);

}
