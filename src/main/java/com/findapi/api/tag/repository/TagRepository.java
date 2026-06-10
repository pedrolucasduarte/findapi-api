package com.findapi.api.tag.repository;

import java.util.Optional;
import java.util.UUID;

import com.findapi.api.entity.TagEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface TagRepository extends JpaRepository<TagEntity, UUID>, JpaSpecificationExecutor<TagEntity> {
    Optional<TagEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<TagEntity> findBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndIdNotAndDeletedAtIsNull(String slug, UUID id);

    long countByDeletedAtIsNull();
}
