package com.findapi.api.apiCatalog.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.findapi.api.entity.ApiTagEntity;
import com.findapi.api.entity.ApiTagId;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ApiTagRepository extends JpaRepository<ApiTagEntity, ApiTagId> {
    Optional<ApiTagEntity> findByApiIdAndTagId(UUID apiId, UUID tagId);

    Optional<ApiTagEntity> findByApiIdAndTagIdAndDeletedAtIsNull(UUID apiId, UUID tagId);

    List<ApiTagEntity> findByApiIdAndDeletedAtIsNull(UUID apiId);
}
