package com.findapi.api.category.repository;

import java.util.Optional;
import java.util.UUID;

import com.findapi.api.entity.CategoryEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CategoryRepository extends JpaRepository<CategoryEntity, UUID>,
        JpaSpecificationExecutor<CategoryEntity> {
    Optional<CategoryEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<CategoryEntity> findBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndIdNotAndDeletedAtIsNull(String slug, UUID id);

    long countByDeletedAtIsNull();
}
