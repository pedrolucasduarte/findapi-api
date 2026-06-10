package com.findapi.api.review.specification;

import java.util.UUID;

import com.findapi.api.entity.ReviewEntity;
import com.findapi.api.review.dto.request.ReviewFilterRequest;

import org.springframework.data.jpa.domain.Specification;

public final class ReviewSpecification {
    private ReviewSpecification() {
    }

    public static Specification<ReviewEntity> deletedAtIsNull() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt"));
    }

    public static Specification<ReviewEntity> apiIdEquals(UUID apiId) {
        return apiId == null
                ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("api").get("id"), apiId);
    }

    public static Specification<ReviewEntity> ratingEquals(Integer rating) {
        return rating == null
                ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("rating"), rating.shortValue());
    }

    public static Specification<ReviewEntity> userIdEquals(UUID userId) {
        return userId == null
                ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), userId);
    }

    public static Specification<ReviewEntity> fromFilter(ReviewFilterRequest filter) {
        Specification<ReviewEntity> specification = deletedAtIsNull();
        if (filter == null) {
            return specification;
        }

        specification = andIfPresent(specification, apiIdEquals(filter.getApiId()));
        specification = andIfPresent(specification, ratingEquals(filter.getRating()));
        return andIfPresent(specification, userIdEquals(filter.getUserId()));
    }

    private static Specification<ReviewEntity> andIfPresent(
            Specification<ReviewEntity> current,
            Specification<ReviewEntity> candidate
    ) {
        return candidate == null ? current : current.and(candidate);
    }
}
