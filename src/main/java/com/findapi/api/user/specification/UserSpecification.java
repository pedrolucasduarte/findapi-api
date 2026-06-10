package com.findapi.api.user.specification;

import java.util.Locale;

import com.findapi.api.entity.AppUserEntity;
import com.findapi.api.enums.UserRole;
import com.findapi.api.user.dto.request.UserFilterRequest;

import org.springframework.data.jpa.domain.Specification;

public final class UserSpecification {
    private UserSpecification() {
    }

    public static Specification<AppUserEntity> deletedAtIsNull() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt"));
    }

    public static Specification<AppUserEntity> nameContains(String name) {
        return hasText(name)
                ? (root, query, criteriaBuilder) -> criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + clean(name).toLowerCase(Locale.ROOT) + "%"
                )
                : null;
    }

    public static Specification<AppUserEntity> emailContains(String email) {
        return hasText(email)
                ? (root, query, criteriaBuilder) -> criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("email")),
                        "%" + clean(email).toLowerCase(Locale.ROOT) + "%"
                )
                : null;
    }

    public static Specification<AppUserEntity> roleEquals(UserRole role) {
        return role == null ? null : (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("role"), role);
    }

    public static Specification<AppUserEntity> fromFilter(UserFilterRequest filter) {
        Specification<AppUserEntity> specification = deletedAtIsNull();
        if (filter == null) {
            return specification;
        }
        specification = andIfPresent(specification, nameContains(filter.getName()));
        specification = andIfPresent(specification, emailContains(filter.getEmail()));
        return andIfPresent(specification, roleEquals(filter.getRole()));
    }

    private static Specification<AppUserEntity> andIfPresent(
            Specification<AppUserEntity> current,
            Specification<AppUserEntity> candidate
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
