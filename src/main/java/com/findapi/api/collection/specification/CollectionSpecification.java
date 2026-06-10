package com.findapi.api.collection.specification;

import java.util.Locale;
import java.util.UUID;

import com.findapi.api.collection.dto.request.CollectionFilterRequest;
import com.findapi.api.entity.CollectionEntity;

import org.springframework.data.jpa.domain.Specification;

public final class CollectionSpecification {
    private CollectionSpecification() {
    }

    public static Specification<CollectionEntity> deletedAtIsNull() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt"));
    }

    public static Specification<CollectionEntity> nameContains(String name) {
        return hasText(name)
                ? (root, query, criteriaBuilder) -> criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + clean(name).toLowerCase(Locale.ROOT) + "%"
                )
                : null;
    }

    public static Specification<CollectionEntity> slugEquals(String slug) {
        return hasText(slug)
                ? (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                        root.get("slug"),
                        clean(slug).toLowerCase(Locale.ROOT)
                )
                : null;
    }

    public static Specification<CollectionEntity> ownerIdEquals(UUID ownerId) {
        return ownerId == null
                ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user").get("id"), ownerId);
    }

    public static Specification<CollectionEntity> fromFilter(CollectionFilterRequest filter) {
        Specification<CollectionEntity> specification = deletedAtIsNull();
        if (filter == null) {
            return specification;
        }

        specification = andIfPresent(specification, nameContains(filter.getName()));
        specification = andIfPresent(specification, slugEquals(filter.getSlug()));
        return andIfPresent(specification, ownerIdEquals(filter.getOwnerId()));
    }

    private static Specification<CollectionEntity> andIfPresent(
            Specification<CollectionEntity> current,
            Specification<CollectionEntity> candidate
    ) {
        return candidate == null ? current : current.and(candidate);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String clean(String value) {
        return value.trim();
    }
}
