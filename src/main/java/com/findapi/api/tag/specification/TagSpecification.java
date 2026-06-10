package com.findapi.api.tag.specification;

import java.util.Locale;

import com.findapi.api.entity.TagEntity;
import com.findapi.api.tag.dto.request.TagFilterRequest;

import org.springframework.data.jpa.domain.Specification;

public final class TagSpecification {
    private TagSpecification() {
    }

    public static Specification<TagEntity> deletedAtIsNull() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt"));
    }

    public static Specification<TagEntity> nameContains(String name) {
        return hasText(name)
                ? (root, query, criteriaBuilder) -> criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + clean(name).toLowerCase(Locale.ROOT) + "%"
                )
                : null;
    }

    public static Specification<TagEntity> slugEquals(String slug) {
        return hasText(slug)
                ? (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                        root.get("slug"),
                        clean(slug).toLowerCase(Locale.ROOT)
                )
                : null;
    }

    public static Specification<TagEntity> fromFilter(TagFilterRequest filter) {
        Specification<TagEntity> specification = deletedAtIsNull();
        if (filter == null) {
            return specification;
        }

        specification = andIfPresent(specification, nameContains(filter.getName()));
        return andIfPresent(specification, slugEquals(filter.getSlug()));
    }

    private static Specification<TagEntity> andIfPresent(
            Specification<TagEntity> current,
            Specification<TagEntity> candidate
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
