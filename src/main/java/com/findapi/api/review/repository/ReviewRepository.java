package com.findapi.api.review.repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.findapi.api.entity.ReviewEntity;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewRepository extends JpaRepository<ReviewEntity, UUID>, JpaSpecificationExecutor<ReviewEntity> {
    Optional<ReviewEntity> findByIdAndDeletedAtIsNull(UUID id);

    List<ReviewEntity> findByApiIdAndDeletedAtIsNull(UUID apiId);

    boolean existsByApiIdAndUserIdAndDeletedAtIsNull(UUID apiId, UUID userId);

    Optional<ReviewEntity> findByApiIdAndUserIdAndDeletedAtIsNull(UUID apiId, UUID userId);

    @Query("""
            select avg(review.rating)
            from ReviewEntity review
            where review.api.id = :apiId
              and review.deletedAt is null
            """)
    Double calculateAverageRating(@Param("apiId") UUID apiId);

    @Query("""
            select count(review)
            from ReviewEntity review
            where review.api.id = :apiId
              and review.deletedAt is null
            """)
    long calculateReviewCount(@Param("apiId") UUID apiId);

    long countByDeletedAtIsNull();

    long countByApiIdAndRatingAndDeletedAtIsNull(UUID apiId, Short rating);

    @Query("""
            select review.api.id as apiId,
                   avg(review.rating) as ratingAverage,
                   count(review) as ratingCount
            from ReviewEntity review
            where review.api.id in :apiIds
              and review.deletedAt is null
            group by review.api.id
            """)
    List<ReviewRatingSummary> summarizeRatings(@Param("apiIds") Collection<UUID> apiIds);
}
