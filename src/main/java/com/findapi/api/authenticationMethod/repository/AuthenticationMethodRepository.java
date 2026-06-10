package com.findapi.api.authenticationMethod.repository;

import java.util.Optional;
import java.util.UUID;

import com.findapi.api.entity.AuthenticationMethodEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AuthenticationMethodRepository extends JpaRepository<AuthenticationMethodEntity, UUID>,
        JpaSpecificationExecutor<AuthenticationMethodEntity> {
    Optional<AuthenticationMethodEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<AuthenticationMethodEntity> findByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndDeletedAtIsNull(String name);

    boolean existsByNameAndIdNotAndDeletedAtIsNull(String name, UUID id);
}
