package com.findapi.api.user.repository;

import java.util.Optional;
import java.util.UUID;

import com.findapi.api.entity.AppUserEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<AppUserEntity, UUID>, JpaSpecificationExecutor<AppUserEntity> {
    Optional<AppUserEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<AppUserEntity> findByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    boolean existsByEmailIgnoreCaseAndDeletedAtIsNull(String email);

    boolean existsByEmailIgnoreCaseAndIdNotAndDeletedAtIsNull(String email, UUID id);
}
