package com.findapi.api.pricing.specification;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.UUID;

import com.findapi.api.entity.PricingPlanEntity;
import com.findapi.api.enums.BillingType;
import com.findapi.api.pricing.dto.request.PricingPlanFilterRequest;

import org.springframework.data.jpa.domain.Specification;

public final class PricingPlanSpecification {
    private PricingPlanSpecification() {
    }

    public static Specification<PricingPlanEntity> deletedAtIsNull() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isNull(root.get("deletedAt"));
    }

    public static Specification<PricingPlanEntity> apiIdEquals(UUID apiId) {
        return apiId == null
                ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("api").get("id"), apiId);
    }

    public static Specification<PricingPlanEntity> nameContains(String name) {
        return hasText(name)
                ? (root, query, criteriaBuilder) -> criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("name")),
                        "%" + clean(name).toLowerCase(Locale.ROOT) + "%"
                )
                : null;
    }

    public static Specification<PricingPlanEntity> billingTypeEquals(BillingType billingType) {
        return billingType == null
                ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("billingType"), billingType);
    }

    public static Specification<PricingPlanEntity> currencyEquals(String currency) {
        return hasText(currency)
                ? (root, query, criteriaBuilder) -> criteriaBuilder.equal(
                        root.get("currency"),
                        clean(currency).toUpperCase(Locale.ROOT)
                )
                : null;
    }

    public static Specification<PricingPlanEntity> priceGreaterThanOrEqual(BigDecimal minPrice) {
        return minPrice == null
                ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.greaterThanOrEqualTo(root.get("price"), minPrice);
    }

    public static Specification<PricingPlanEntity> priceLessThanOrEqual(BigDecimal maxPrice) {
        return maxPrice == null
                ? null
                : (root, query, criteriaBuilder) -> criteriaBuilder.lessThanOrEqualTo(root.get("price"), maxPrice);
    }

    public static Specification<PricingPlanEntity> fromFilter(PricingPlanFilterRequest filter) {
        Specification<PricingPlanEntity> specification = deletedAtIsNull();
        if (filter == null) {
            return specification;
        }

        specification = andIfPresent(specification, apiIdEquals(filter.getApiId()));
        specification = andIfPresent(specification, nameContains(filter.getName()));
        specification = andIfPresent(specification, billingTypeEquals(filter.getBillingType()));
        specification = andIfPresent(specification, currencyEquals(filter.getCurrency()));
        specification = andIfPresent(specification, priceGreaterThanOrEqual(filter.getMinPrice()));
        return andIfPresent(specification, priceLessThanOrEqual(filter.getMaxPrice()));
    }

    private static Specification<PricingPlanEntity> andIfPresent(
            Specification<PricingPlanEntity> current,
            Specification<PricingPlanEntity> candidate
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
