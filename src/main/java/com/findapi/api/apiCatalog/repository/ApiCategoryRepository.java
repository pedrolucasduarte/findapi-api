package com.findapi.api.apiCatalog.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.findapi.api.entity.ApiCategoryEntity;
import com.findapi.api.entity.ApiCategoryId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiCategoryRepository extends JpaRepository<ApiCategoryEntity, ApiCategoryId> {
    Optional<ApiCategoryEntity> findByApiIdAndCategoryId(UUID apiId, UUID categoryId);

    Optional<ApiCategoryEntity> findByApiIdAndCategoryIdAndDeletedAtIsNull(UUID apiId, UUID categoryId);

    List<ApiCategoryEntity> findByApiIdAndDeletedAtIsNull(UUID apiId);
}
