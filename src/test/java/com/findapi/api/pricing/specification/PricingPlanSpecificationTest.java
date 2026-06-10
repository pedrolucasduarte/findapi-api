package com.findapi.api.pricing.specification;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.findapi.api.TestcontainersConfiguration;
import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.entity.AuthenticationMethodEntity;
import com.findapi.api.entity.PricingPlanEntity;
import com.findapi.api.enums.ApiStatus;
import com.findapi.api.enums.ApiType;
import com.findapi.api.enums.BillingType;
import com.findapi.api.enums.IntegrationDifficulty;
import com.findapi.api.pricing.dto.request.PricingPlanFilterRequest;
import com.findapi.api.pricing.repository.PricingPlanRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class PricingPlanSpecificationTest {
    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private PricingPlanRepository pricingPlanRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void filtersByApiId() {
        AuthenticationMethodEntity authenticationMethod = authenticationMethod();
        ApiEntity weatherApi = saveApi("Open Weather", "pricing-open-weather", authenticationMethod);
        ApiEntity paymentApi = saveApi("Payment Hub", "pricing-payment-hub", authenticationMethod);
        PricingPlanEntity target = savePlan(weatherApi, "Starter", BillingType.SUBSCRIPTION, new BigDecimal("29.90"), "USD");
        savePlan(paymentApi, "Starter", BillingType.SUBSCRIPTION, new BigDecimal("39.90"), "USD");

        PricingPlanFilterRequest filter = PricingPlanFilterRequest.builder()
                .apiId(weatherApi.getId())
                .build();

        List<PricingPlanEntity> result = pricingPlanRepository.findAll(PricingPlanSpecification.fromFilter(filter));

        assertThat(result).extracting(PricingPlanEntity::getId).containsExactly(target.getId());
    }

    @Test
    void filtersByBillingTypeAndCurrency() {
        AuthenticationMethodEntity authenticationMethod = authenticationMethod();
        ApiEntity api = saveApi("Billing API", "pricing-billing-api", authenticationMethod);
        PricingPlanEntity target = savePlan(api, "Growth", BillingType.SUBSCRIPTION, new BigDecimal("49.90"), "BRL");
        savePlan(api, "Free", BillingType.FREE, BigDecimal.ZERO, "USD");

        PricingPlanFilterRequest filter = PricingPlanFilterRequest.builder()
                .billingType(BillingType.SUBSCRIPTION)
                .currency(" brl ")
                .build();

        List<PricingPlanEntity> result = pricingPlanRepository.findAll(PricingPlanSpecification.fromFilter(filter));

        assertThat(result).extracting(PricingPlanEntity::getId).containsExactly(target.getId());
    }

    @Test
    void filtersByPriceRange() {
        AuthenticationMethodEntity authenticationMethod = authenticationMethod();
        ApiEntity api = saveApi("Range API", "pricing-range-api", authenticationMethod);
        savePlan(api, "Low", BillingType.USAGE_BASED, new BigDecimal("10.00"), "USD");
        PricingPlanEntity target = savePlan(api, "Middle", BillingType.USAGE_BASED, new BigDecimal("50.00"), "USD");
        savePlan(api, "High", BillingType.USAGE_BASED, new BigDecimal("100.00"), "USD");

        PricingPlanFilterRequest filter = PricingPlanFilterRequest.builder()
                .minPrice(new BigDecimal("20.00"))
                .maxPrice(new BigDecimal("80.00"))
                .build();

        List<PricingPlanEntity> result = pricingPlanRepository.findAll(PricingPlanSpecification.fromFilter(filter));

        assertThat(result).extracting(PricingPlanEntity::getId).containsExactly(target.getId());
    }

    @Test
    void alwaysAppliesDeletedAtIsNull() {
        AuthenticationMethodEntity authenticationMethod = authenticationMethod();
        ApiEntity api = saveApi("Soft Delete API", "pricing-soft-delete-api", authenticationMethod);
        PricingPlanEntity active = savePlan(api, "Active", BillingType.SUBSCRIPTION, new BigDecimal("29.90"), "USD");
        PricingPlanEntity deleted = savePlan(api, "Deleted", BillingType.SUBSCRIPTION, new BigDecimal("39.90"), "USD");
        deleted.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        pricingPlanRepository.saveAndFlush(deleted);

        List<PricingPlanEntity> result = pricingPlanRepository.findAll(PricingPlanSpecification.fromFilter(null));

        assertThat(result).extracting(PricingPlanEntity::getId).contains(active.getId());
        assertThat(result).extracting(PricingPlanEntity::getId).doesNotContain(deleted.getId());
    }

    private PricingPlanEntity savePlan(
            ApiEntity api,
            String name,
            BillingType billingType,
            BigDecimal price,
            String currency
    ) {
        PricingPlanEntity entity = new PricingPlanEntity();
        entity.setApi(api);
        entity.setName(name);
        entity.setBillingType(billingType);
        entity.setPrice(price);
        entity.setCurrency(currency);
        return pricingPlanRepository.saveAndFlush(entity);
    }

    private ApiEntity saveApi(String name, String slug, AuthenticationMethodEntity authenticationMethod) {
        ApiEntity entity = new ApiEntity();
        entity.setAuthenticationMethod(authenticationMethod);
        entity.setName(name);
        entity.setSlug(slug);
        entity.setShortDescription("Public integration API");
        entity.setFullDescription("Detailed description");
        entity.setOfficialSite("https://example.com");
        entity.setDocumentationUrl("https://docs.example.com");
        entity.setApiType(ApiType.PUBLIC);
        entity.setStatus(ApiStatus.ACTIVE);
        entity.setFreeTier(true);
        entity.setOfficialSdk(true);
        entity.setOpenSource(false);
        entity.setSelfHosted(false);
        entity.setBrazilian(false);
        entity.setIntegrationDifficulty(IntegrationDifficulty.EASY);
        return apiRepository.saveAndFlush(entity);
    }

    private AuthenticationMethodEntity authenticationMethod() {
        return entityManager
                .createQuery(
                        "select method from AuthenticationMethodEntity method where method.name = :name",
                        AuthenticationMethodEntity.class
                )
                .setParameter("name", "API_KEY")
                .getSingleResult();
    }
}
