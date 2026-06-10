package com.findapi.api.apiCatalog.repository;

import java.util.Optional;
import java.util.UUID;

import com.findapi.api.entity.ApiEntity;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface ApiRepository extends JpaRepository<ApiEntity, UUID>, JpaSpecificationExecutor<ApiEntity> {
    boolean existsBySlugAndDeletedAtIsNull(String slug);

    boolean existsBySlugAndIdNotAndDeletedAtIsNull(String slug, UUID id);

    Optional<ApiEntity> findByIdAndDeletedAtIsNull(UUID id);

    Optional<ApiEntity> findBySlugAndDeletedAtIsNull(String slug);

    long countByDeletedAtIsNull();

    long countByBrazilianTrueAndDeletedAtIsNull();

    Page<ApiEntity> findByDeletedAtIsNull(Pageable pageable);

    Page<ApiEntity> findByFreeTierTrueAndDeletedAtIsNull(Pageable pageable);

    Page<ApiEntity> findByOpenSourceTrueAndDeletedAtIsNull(Pageable pageable);

    Page<ApiEntity> findByBrazilianTrueAndDeletedAtIsNull(Pageable pageable);

    @Query("""
            select api
            from ApiEntity api
            left join ReviewEntity review on review.api = api and review.deletedAt is null
            where api.deletedAt is null
            group by api
            order by coalesce(avg(review.rating), 0) desc, count(review) desc, api.name asc
            """)
    Page<ApiEntity> findTopRated(Pageable pageable);

    @Query("""
            select api
            from ApiEntity api
            left join ReviewEntity review on review.api = api and review.deletedAt is null
            where api.deletedAt is null
            group by api
            order by count(review) desc, api.createdAt desc
            """)
    Page<ApiEntity> findTrending(Pageable pageable);
}
