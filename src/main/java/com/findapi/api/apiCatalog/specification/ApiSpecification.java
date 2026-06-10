package com.findapi.api.apiCatalog.specification;

import java.util.Locale;
import java.util.UUID;

import com.findapi.api.apiCatalog.dto.request.ApiFilterRequest;
import com.findapi.api.entity.ApiCategoryEntity;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.entity.ApiTagEntity;
import com.findapi.api.enums.ApiStatus;
import com.findapi.api.enums.ApiType;
import com.findapi.api.enums.IntegrationDifficulty;

import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;

public final class ApiSpecification {
    private ApiSpecification() {
    }

    public static Specification<ApiEntity> deletedAtIsNull() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt"));
    }

    public static Specification<ApiEntity> nameContains(String name) {
        return hasText(name)
                ? (root, query, criteriaBuilder) -> criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + clean(name).toLowerCase(Locale.ROOT) + "%"
                )
                : null;
    }

    public static Specification<ApiEntity> slugEquals(String slug) {
        return hasText(slug)
                ? (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                        root.get("slug"),
                        clean(slug).toLowerCase(Locale.ROOT)
                )
                : null;
    }

    public static Specification<ApiEntity> apiTypeEquals(ApiType apiType) {
        return equalsAttribute("apiType", apiType);
    }

    public static Specification<ApiEntity> statusEquals(ApiStatus status) {
        return equalsAttribute("status", status);
    }

    public static Specification<ApiEntity> freeTierEquals(Boolean freeTier) {
        return equalsAttribute("freeTier", freeTier);
    }

    public static Specification<ApiEntity> officialSdkEquals(Boolean officialSdk) {
        return equalsAttribute("officialSdk", officialSdk);
    }

    public static Specification<ApiEntity> openSourceEquals(Boolean openSource) {
        return equalsAttribute("openSource", openSource);
    }

    public static Specification<ApiEntity> selfHostedEquals(Boolean selfHosted) {
        return equalsAttribute("selfHosted", selfHosted);
    }

    public static Specification<ApiEntity> brazilianEquals(Boolean brazilian) {
        return equalsAttribute("brazilian", brazilian);
    }

    public static Specification<ApiEntity> integrationDifficultyEquals(
            IntegrationDifficulty integrationDifficulty
    ) {
        return equalsAttribute("integrationDifficulty", integrationDifficulty);
    }

    public static Specification<ApiEntity> authenticationMethodEquals(UUID authenticationMethodId) {
        return authenticationMethodId == null
                ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                        root.get("authenticationMethod").get("id"),
                        authenticationMethodId
                );
    }

    public static Specification<ApiEntity> categoryEquals(UUID categoryId) {
        return categoryId == null ? null : (root, query, criteriaBuilder) -> {
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<ApiCategoryEntity> association = subquery.from(ApiCategoryEntity.class);
            subquery.select(association.get("api").get("id"));
            subquery.where(
                    criteriaBuilder.equal(association.get("category").get("id"), categoryId),
                    criteriaBuilder.isNull(association.get("deletedAt")),
                    criteriaBuilder.isNull(association.get("category").get("deletedAt"))
            );
            return root.get("id").in(subquery);
        };
    }

    public static Specification<ApiEntity> tagEquals(UUID tagId) {
        return tagId == null ? null : (root, query, criteriaBuilder) -> {
            Subquery<UUID> subquery = query.subquery(UUID.class);
            Root<ApiTagEntity> association = subquery.from(ApiTagEntity.class);
            subquery.select(association.get("api").get("id"));
            subquery.where(
                    criteriaBuilder.equal(association.get("tag").get("id"), tagId),
                    criteriaBuilder.isNull(association.get("deletedAt")),
                    criteriaBuilder.isNull(association.get("tag").get("deletedAt"))
            );
            return root.get("id").in(subquery);
        };
    }

    public static Specification<ApiEntity> fromFilter(ApiFilterRequest filter) {
        Specification<ApiEntity> specification = deletedAtIsNull();
        if (filter == null) {
            return specification;
        }

        specification = andIfPresent(specification, nameContains(filter.getName()));
        specification = andIfPresent(specification, slugEquals(filter.getSlug()));
        specification = andIfPresent(specification, apiTypeEquals(filter.getApiType()));
        specification = andIfPresent(specification, statusEquals(filter.getStatus()));
        specification = andIfPresent(specification, freeTierEquals(filter.getFreeTier()));
        specification = andIfPresent(specification, officialSdkEquals(filter.getOfficialSdk()));
        specification = andIfPresent(specification, openSourceEquals(filter.getOpenSource()));
        specification = andIfPresent(specification, selfHostedEquals(filter.getSelfHosted()));
        specification = andIfPresent(specification, brazilianEquals(filter.getBrazilian()));
        specification = andIfPresent(
                specification,
                integrationDifficultyEquals(filter.getIntegrationDifficulty())
        );
        return andIfPresent(specification, authenticationMethodEquals(filter.getAuthenticationMethodId()));
    }

    private static Specification<ApiEntity> andIfPresent(
            Specification<ApiEntity> current,
            Specification<ApiEntity> candidate
    ) {
        return candidate == null ? current : current.and(candidate);
    }

    private static <T> Specification<ApiEntity> equalsAttribute(String attribute, T value) {
        return value == null
                ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get(attribute), value);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static String clean(String value) {
        return value.trim();
    }
}
