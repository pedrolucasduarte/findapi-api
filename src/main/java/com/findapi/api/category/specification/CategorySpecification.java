package com.findapi.api.category.specification;

import java.util.Locale;

import com.findapi.api.category.dto.request.CategoryFilterRequest;
import com.findapi.api.entity.CategoryEntity;

import org.springframework.data.jpa.domain.Specification;

public final class CategorySpecification {
    private CategorySpecification() {
    }

    public static Specification<CategoryEntity> deletedAtIsNull() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt"));
    }

    public static Specification<CategoryEntity> nameContains(String name) {
        return hasText(name)
                ? (root, query, criteriaBuilder) -> criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + clean(name).toLowerCase(Locale.ROOT) + "%"
                )
                : null;
    }

    public static Specification<CategoryEntity> slugEquals(String slug) {
        return hasText(slug)
                ? (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                        root.get("slug"),
                        clean(slug).toLowerCase(Locale.ROOT)
                )
                : null;
    }

    public static Specification<CategoryEntity> fromFilter(CategoryFilterRequest filter) {
        Specification<CategoryEntity> specification = deletedAtIsNull();
        if (filter == null) {
            return specification;
        }

        specification = andIfPresent(specification, nameContains(filter.getName()));
        return andIfPresent(specification, slugEquals(filter.getSlug()));
    }

    private static Specification<CategoryEntity> andIfPresent(
            Specification<CategoryEntity> current,
            Specification<CategoryEntity> candidate
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
