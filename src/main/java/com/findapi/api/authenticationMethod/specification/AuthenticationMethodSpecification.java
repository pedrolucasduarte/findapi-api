package com.findapi.api.authenticationMethod.specification;

import java.util.Locale;

import com.findapi.api.authenticationMethod.dto.request.AuthenticationMethodFilterRequest;
import com.findapi.api.entity.AuthenticationMethodEntity;

import org.springframework.data.jpa.domain.Specification;

public final class AuthenticationMethodSpecification {
    private AuthenticationMethodSpecification() {
    }

    public static Specification<AuthenticationMethodEntity> deletedAtIsNull() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt"));
    }

    public static Specification<AuthenticationMethodEntity> nameContains(String name) {
        return hasText(name)
                ? (root, query, criteriaBuilder) -> criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + clean(name).toLowerCase(Locale.ROOT) + "%"
                )
                : null;
    }

    public static Specification<AuthenticationMethodEntity> nameEquals(String name) {
        return hasText(name)
                ? (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                        root.get("name"),
                        clean(name).toUpperCase(Locale.ROOT)
                )
                : null;
    }

    public static Specification<AuthenticationMethodEntity> fromFilter(AuthenticationMethodFilterRequest filter) {
        Specification<AuthenticationMethodEntity> specification = deletedAtIsNull();
        if (filter == null) {
            return specification;
        }

        return andIfPresent(specification, nameContains(filter.getName()));
    }

    private static Specification<AuthenticationMethodEntity> andIfPresent(
            Specification<AuthenticationMethodEntity> current,
            Specification<AuthenticationMethodEntity> candidate
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
