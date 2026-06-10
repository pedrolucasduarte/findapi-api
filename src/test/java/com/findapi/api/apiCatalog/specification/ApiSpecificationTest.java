package com.findapi.api.apiCatalog.specification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;

import com.findapi.api.TestcontainersConfiguration;
import com.findapi.api.apiCatalog.dto.request.ApiFilterRequest;
import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.entity.AuthenticationMethodEntity;
import com.findapi.api.enums.ApiStatus;
import com.findapi.api.enums.ApiType;
import com.findapi.api.enums.IntegrationDifficulty;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class ApiSpecificationTest {
    @Autowired
    private ApiRepository apiRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void filtersByNameAndIgnoresDeletedApis() {
        AuthenticationMethodEntity authenticationMethod = authenticationMethod();
        ApiEntity active = saveApi("Open Weather", "open-weather", authenticationMethod);
        ApiEntity deleted = saveApi("Weather Legacy", "weather-legacy", authenticationMethod);
        deleted.markDeleted(Instant.now());
        apiRepository.saveAndFlush(deleted);

        ApiFilterRequest filter = ApiFilterRequest.builder()
                .name(" weather ")
                .build();

        List<ApiEntity> result = apiRepository.findAll(ApiSpecification.fromFilter(filter));

        assertThat(result).extracting(ApiEntity::getId).containsExactly(active.getId());
    }

    @Test
    void combinesTechnicalFilters() {
        AuthenticationMethodEntity authenticationMethod = authenticationMethod();
        ApiEntity target = saveApi("Open Weather", "open-weather", authenticationMethod);
        saveApi("Payment Hub", "payment-hub", authenticationMethod);

        ApiFilterRequest filter = ApiFilterRequest.builder()
                .slug(" OPEN-WEATHER ")
                .apiType(ApiType.PUBLIC)
                .status(ApiStatus.ACTIVE)
                .freeTier(true)
                .officialSdk(true)
                .openSource(false)
                .selfHosted(false)
                .brazilian(false)
                .integrationDifficulty(IntegrationDifficulty.EASY)
                .authenticationMethodId(authenticationMethod.getId())
                .build();

        List<ApiEntity> result = apiRepository.findAll(ApiSpecification.fromFilter(filter));

        assertThat(result).extracting(ApiEntity::getId).containsExactly(target.getId());
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
