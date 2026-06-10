package com.findapi.api.apiCatalog.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.findapi.api.apiCatalog.dto.request.ApiCreateRequest;
import com.findapi.api.apiCatalog.dto.request.ApiFilterRequest;
import com.findapi.api.apiCatalog.dto.request.ApiUpdateRequest;
import com.findapi.api.apiCatalog.dto.response.ApiDetailResponse;
import com.findapi.api.apiCatalog.dto.response.ApiResponse;
import com.findapi.api.apiCatalog.mapper.ApiCatalogMapper;
import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.common.exception.ResourceNotFoundException;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.entity.AuthenticationMethodEntity;
import com.findapi.api.enums.ApiStatus;
import com.findapi.api.enums.ApiType;
import com.findapi.api.enums.IntegrationDifficulty;
import com.findapi.api.review.repository.ReviewRepository;

import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ApiCatalogServiceTest {
    private static final UUID API_ID = UUID.fromString("3b383a91-c940-46a4-89fe-8f912558ac8f");
    private static final UUID AUTHENTICATION_METHOD_ID = UUID.fromString("7cb2ff94-69d5-4da1-88b1-84d54dc3948d");

    @Mock
    private ApiRepository apiRepository;

    @Mock
    private ApiCatalogMapper apiCatalogMapper;

    @Mock
    private EntityManager entityManager;

    @Mock
    private ReviewRepository reviewRepository;

    private ApiCatalogService service;

    @BeforeEach
    void setUp() {
        service = new ApiCatalogService(apiRepository, apiCatalogMapper, entityManager, reviewRepository);
    }

    @Test
    void createApiSuccessfully() {
        ApiCreateRequest request = createRequest("Open-Weather");
        ApiDetailResponse expected = detailResponse("open-weather");

        when(apiRepository.existsBySlugAndDeletedAtIsNull("open-weather")).thenReturn(false);
        when(entityManager.find(AuthenticationMethodEntity.class, AUTHENTICATION_METHOD_ID))
                .thenReturn(authenticationMethod());
        when(apiRepository.save(any(ApiEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(apiCatalogMapper.toDetailResponse(any(ApiEntity.class))).thenReturn(expected);

        ApiDetailResponse response = service.create(request);

        ArgumentCaptor<ApiEntity> captor = ArgumentCaptor.forClass(ApiEntity.class);
        verify(apiRepository).save(captor.capture());
        assertThat(response).isEqualTo(expected);
        assertThat(captor.getValue().getSlug()).isEqualTo("open-weather");
        assertThat(captor.getValue().getName()).isEqualTo("Open Weather");
        assertThat(captor.getValue().getDeletedAt()).isNull();
    }

    @Test
    void blockDuplicatedSlugOnCreate() {
        ApiCreateRequest request = createRequest("open-weather");
        when(apiRepository.existsBySlugAndDeletedAtIsNull("open-weather")).thenReturn(true);

        assertThatThrownBy(() -> service.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("API slug already exists.");
    }

    @Test
    void findApiById() {
        ApiEntity entity = activeEntity("open-weather");
        ApiDetailResponse expected = detailResponse("open-weather");

        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.of(entity));
        when(apiCatalogMapper.toDetailResponse(entity)).thenReturn(expected);
        when(reviewRepository.calculateAverageRating(API_ID)).thenReturn(4.25);
        when(reviewRepository.calculateReviewCount(API_ID)).thenReturn(8L);

        ApiDetailResponse response = service.findById(API_ID);

        assertThat(response).isEqualTo(expected);
        assertThat(response.getRatingAverage()).isEqualTo(4.25);
        assertThat(response.getRatingCount()).isEqualTo(8);
    }

    @Test
    void returnNotFoundWhenApiIdDoesNotExist() {
        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(API_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("API not found.");
    }

    @Test
    void findApiBySlug() {
        ApiEntity entity = activeEntity("open-weather");
        ApiDetailResponse expected = detailResponse("open-weather");

        when(apiRepository.findBySlugAndDeletedAtIsNull("open-weather")).thenReturn(Optional.of(entity));
        when(apiCatalogMapper.toDetailResponse(entity)).thenReturn(expected);

        assertThat(service.findBySlug("Open-Weather")).isEqualTo(expected);
    }

    @Test
    void returnNotFoundWhenApiSlugDoesNotExist() {
        when(apiRepository.findBySlugAndDeletedAtIsNull("missing-api")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findBySlug("Missing-Api"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("API not found.");
    }

    @Test
    void listPagedApisWithFilters() {
        ApiEntity entity = activeEntity("open-weather");
        ApiResponse mapped = response("open-weather");
        PageRequest request = PageRequest.of(0, 200, Sort.unsorted());
        ApiFilterRequest filter = ApiFilterRequest.builder()
                .name("weather")
                .freeTier(true)
                .build();

        when(apiRepository.findAll(
                ArgumentMatchers.<Specification<ApiEntity>>any(),
                any(PageRequest.class)))
                .thenReturn(new PageImpl<>(
                        List.of(entity),
                        PageRequest.of(0, 100, Sort.by("createdAt").descending()),
                        1
                ));
        when(apiCatalogMapper.toResponse(entity)).thenReturn(mapped);

        PageResponse<ApiResponse> page = service.list(filter, request);

        assertThat(page.content()).containsExactly(mapped);
        assertThat(page.size()).isEqualTo(100);
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void updateApiSuccessfully() {
        ApiEntity entity = activeEntity("old-slug");
        ApiUpdateRequest request = updateRequest("New-Slug");
        ApiDetailResponse expected = detailResponse("new-slug");

        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.of(entity));
        when(apiRepository.existsBySlugAndIdNotAndDeletedAtIsNull("new-slug", API_ID)).thenReturn(false);
        when(entityManager.find(AuthenticationMethodEntity.class, AUTHENTICATION_METHOD_ID))
                .thenReturn(authenticationMethod());
        when(apiRepository.save(entity)).thenReturn(entity);
        when(apiCatalogMapper.toDetailResponse(entity)).thenReturn(expected);

        ApiDetailResponse response = service.update(API_ID, request);

        assertThat(response).isEqualTo(expected);
        assertThat(entity.getSlug()).isEqualTo("new-slug");
        assertThat(entity.getName()).isEqualTo("Updated API");
    }

    @Test
    void blockDuplicatedSlugOnUpdate() {
        ApiEntity entity = activeEntity("old-slug");
        ApiUpdateRequest request = updateRequest("existing-api");

        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.of(entity));
        when(apiRepository.existsBySlugAndIdNotAndDeletedAtIsNull("existing-api", API_ID)).thenReturn(true);

        assertThatThrownBy(() -> service.update(API_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("API slug already exists.");
    }

    @Test
    void removeApiWithSoftDelete() {
        ApiEntity entity = activeEntity("open-weather");
        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.of(entity));

        service.delete(API_ID);

        assertThat(entity.getDeletedAt()).isNotNull();
        verify(apiRepository).save(entity);
    }

    private ApiCreateRequest createRequest(String slug) {
        return ApiCreateRequest.builder()
                .name("Open Weather")
                .slug(slug)
                .shortDescription("Weather API")
                .fullDescription("Weather API details")
                .officialSite("https://example.com")
                .documentationUrl("https://docs.example.com")
                .apiType(ApiType.PUBLIC)
                .status(ApiStatus.ACTIVE)
                .freeTier(true)
                .officialSdk(true)
                .openSource(false)
                .selfHosted(false)
                .brazilian(false)
                .integrationDifficulty(IntegrationDifficulty.EASY)
                .authenticationMethodId(AUTHENTICATION_METHOD_ID)
                .build();
    }

    private ApiUpdateRequest updateRequest(String slug) {
        return ApiUpdateRequest.builder()
                .name("Updated API")
                .slug(slug)
                .shortDescription("Updated short description")
                .fullDescription("Updated details")
                .officialSite("https://updated.example.com")
                .documentationUrl("https://docs.updated.example.com")
                .apiType(ApiType.FREEMIUM)
                .status(ApiStatus.ACTIVE)
                .freeTier(true)
                .officialSdk(false)
                .openSource(false)
                .selfHosted(false)
                .brazilian(true)
                .integrationDifficulty(IntegrationDifficulty.MEDIUM)
                .authenticationMethodId(AUTHENTICATION_METHOD_ID)
                .build();
    }

    private AuthenticationMethodEntity authenticationMethod() {
        AuthenticationMethodEntity authenticationMethod = new AuthenticationMethodEntity();
        authenticationMethod.setId(AUTHENTICATION_METHOD_ID);
        authenticationMethod.setName("API_KEY");
        return authenticationMethod;
    }

    private ApiEntity activeEntity(String slug) {
        ApiEntity entity = new ApiEntity();
        entity.setId(API_ID);
        entity.setName("Open Weather");
        entity.setSlug(slug);
        entity.setShortDescription("Weather API");
        entity.setApiType(ApiType.PUBLIC);
        entity.setStatus(ApiStatus.ACTIVE);
        entity.setIntegrationDifficulty(IntegrationDifficulty.EASY);
        entity.setAuthenticationMethod(authenticationMethod());
        return entity;
    }

    private ApiDetailResponse detailResponse(String slug) {
        return ApiDetailResponse.builder()
                .id(API_ID)
                .name("Open Weather")
                .slug(slug)
                .shortDescription("Weather API")
                .fullDescription("Weather API details")
                .officialSite("https://example.com")
                .documentationUrl("https://docs.example.com")
                .apiType(ApiType.PUBLIC)
                .status(ApiStatus.ACTIVE)
                .freeTier(true)
                .officialSdk(true)
                .openSource(false)
                .selfHosted(false)
                .brazilian(false)
                .integrationDifficulty(IntegrationDifficulty.EASY)
                .authenticationMethodId(AUTHENTICATION_METHOD_ID)
                .authenticationMethodName("API_KEY")
                .build();
    }

    private ApiResponse response(String slug) {
        return ApiResponse.builder()
                .id(API_ID)
                .name("Open Weather")
                .slug(slug)
                .shortDescription("Weather API")
                .apiType(ApiType.PUBLIC)
                .status(ApiStatus.ACTIVE)
                .freeTier(true)
                .officialSdk(true)
                .openSource(false)
                .selfHosted(false)
                .brazilian(false)
                .integrationDifficulty(IntegrationDifficulty.EASY)
                .authenticationMethodId(AUTHENTICATION_METHOD_ID)
                .build();
    }
}
