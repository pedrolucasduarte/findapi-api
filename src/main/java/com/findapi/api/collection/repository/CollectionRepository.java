package com.findapi.api.collection.repository;

import java.util.Optional;
import java.util.UUID;

import com.findapi.api.entity.CollectionEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CollectionRepository extends JpaRepository<CollectionEntity, UUID>,
        JpaSpecificationExecutor<CollectionEntity> {
    Optional<CollectionEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<CollectionEntity> findBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndIdNotAndDeletedAtIsNull(String slug, UUID id);

    long countByDeletedAtIsNull();
}
