package com.bhavaniprasad.moneymanager.repository;

import com.bhavaniprasad.moneymanager.entity.ProfileEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProfileRepository extends JpaRepository<ProfileEntity,Long>{

    //select * from tbl_profiles where email = ?
    Optional<ProfileEntity> findByEmail(String email);

    //select * from tbl_profiles where activation_token = ?
    Optional<ProfileEntity> findByActivationToken(String activationToken);

    List<ProfileEntity> findAllByOrderByCreatedAtDesc();

    boolean existsByEmail(String email);
}
