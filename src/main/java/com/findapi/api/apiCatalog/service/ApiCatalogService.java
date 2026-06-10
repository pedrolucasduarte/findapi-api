package com.findapi.api.apiCatalog.service;

import java.time.Instant;
import java.util.UUID;

import com.findapi.api.apiCatalog.dto.request.ApiCreateRequest;
import com.findapi.api.apiCatalog.dto.request.ApiFilterRequest;
import com.findapi.api.apiCatalog.dto.request.ApiUpdateRequest;
import com.findapi.api.apiCatalog.dto.response.ApiDetailResponse;
import com.findapi.api.apiCatalog.dto.response.ApiResponse;
import com.findapi.api.apiCatalog.mapper.ApiCatalogMapper;
import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.apiCatalog.specification.ApiSpecification;
import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.common.exception.ResourceNotFoundException;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.common.util.SlugUtils;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.entity.AuthenticationMethodEntity;
import com.findapi.api.review.repository.ReviewRepository;

import jakarta.persistence.EntityManager;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ApiCatalogService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String API_NOT_FOUND = "API not found.";
    private static final String SLUG_ALREADY_EXISTS = "API slug already exists.";

    private final ApiRepository apiRepository;
    private final ApiCatalogMapper apiCatalogMapper;
    private final EntityManager entityManager;
    private final ReviewRepository reviewRepository;

    public ApiCatalogService(
            ApiRepository apiRepository,
            ApiCatalogMapper apiCatalogMapper,
            EntityManager entityManager,
            ReviewRepository reviewRepository
    ) {
        this.apiRepository = apiRepository;
        this.apiCatalogMapper = apiCatalogMapper;
        this.entityManager = entityManager;
        this.reviewRepository = reviewRepository;
    }

    @Transactional
    @CacheEvict(cacheNames = {"dashboard", "rankings"}, allEntries = true)
    public ApiDetailResponse create(ApiCreateRequest request) {
        String normalizedSlug = normalizeSlug(request.getSlug());
        assertSlugAvailable(normalizedSlug);

        ApiEntity entity = new ApiEntity();
        applyCreateRequest(entity, request, normalizedSlug);

        return toDetailResponse(apiRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public ApiDetailResponse findById(UUID id) {
        return toDetailResponse(findActiveEntity(id));
    }

    @Transactional(readOnly = true)
    public ApiDetailResponse findBySlug(String slug) {
        return apiRepository.findBySlugAndDeletedAtIsNull(normalizeSlug(slug))
                .map(this::toDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException(API_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PageResponse<ApiResponse> list(ApiFilterRequest filter, Pageable pageable) {
        Pageable safePageable = safePageable(pageable);
        return PageResponse.from(apiRepository.findAll(ApiSpecification.fromFilter(filter), safePageable)
                .map(apiCatalogMapper::toResponse));
    }

    @Transactional
    @CacheEvict(cacheNames = {"dashboard", "rankings"}, allEntries = true)
    public ApiDetailResponse update(UUID id, ApiUpdateRequest request) {
        ApiEntity entity = findActiveEntity(id);
        String normalizedSlug = normalizeSlug(request.getSlug());

        if (apiRepository.existsBySlugAndIdNotAndDeletedAtIsNull(normalizedSlug, id)) {
            throw new BusinessException(SLUG_ALREADY_EXISTS);
        }

        applyUpdateRequest(entity, request, normalizedSlug);
        return toDetailResponse(apiRepository.save(entity));
    }

    @Transactional
    @CacheEvict(cacheNames = {"dashboard", "rankings"}, allEntries = true)
    public void delete(UUID id) {
        ApiEntity entity = findActiveEntity(id);
        entity.markDeleted(Instant.now());
        apiRepository.save(entity);
    }

    private ApiEntity findActiveEntity(UUID id) {
        return apiRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(API_NOT_FOUND));
    }

    private void assertSlugAvailable(String slug) {
        if (apiRepository.existsBySlugAndDeletedAtIsNull(slug)) {
            throw new BusinessException(SLUG_ALREADY_EXISTS);
        }
    }

    private void applyCreateRequest(ApiEntity entity, ApiCreateRequest request, String normalizedSlug) {
        entity.setAuthenticationMethod(findAuthenticationMethod(request.getAuthenticationMethodId()));
        entity.setName(clean(request.getName()));
        entity.setSlug(normalizedSlug);
        entity.setShortDescription(clean(request.getShortDescription()));
        entity.setFullDescription(clean(request.getFullDescription()));
        entity.setOfficialSite(clean(request.getOfficialSite()));
        entity.setDocumentationUrl(clean(request.getDocumentationUrl()));
        entity.setApiType(request.getApiType());
        entity.setStatus(request.getStatus());
        entity.setFreeTier(request.isFreeTier());
        entity.setOfficialSdk(request.isOfficialSdk());
        entity.setOpenSource(request.isOpenSource());
        entity.setSelfHosted(request.isSelfHosted());
        entity.setBrazilian(request.isBrazilian());
        entity.setIntegrationDifficulty(request.getIntegrationDifficulty());
    }

    private void applyUpdateRequest(ApiEntity entity, ApiUpdateRequest request, String normalizedSlug) {
        entity.setAuthenticationMethod(findAuthenticationMethod(request.getAuthenticationMethodId()));
        entity.setName(clean(request.getName()));
        entity.setSlug(normalizedSlug);
        entity.setShortDescription(clean(request.getShortDescription()));
        entity.setFullDescription(clean(request.getFullDescription()));
        entity.setOfficialSite(clean(request.getOfficialSite()));
        entity.setDocumentationUrl(clean(request.getDocumentationUrl()));
        entity.setApiType(request.getApiType());
        entity.setStatus(request.getStatus());
        entity.setFreeTier(request.isFreeTier());
        entity.setOfficialSdk(request.isOfficialSdk());
        entity.setOpenSource(request.isOpenSource());
        entity.setSelfHosted(request.isSelfHosted());
        entity.setBrazilian(request.isBrazilian());
        entity.setIntegrationDifficulty(request.getIntegrationDifficulty());
    }

    private AuthenticationMethodEntity findAuthenticationMethod(UUID id) {
        AuthenticationMethodEntity authenticationMethod = entityManager.find(AuthenticationMethodEntity.class, id);
        if (authenticationMethod == null || authenticationMethod.getDeletedAt() != null) {
            throw new ResourceNotFoundException("Authentication method not found.");
        }
        return authenticationMethod;
    }

    private Pageable safePageable(Pageable pageable) {
        if (pageable.isUnpaged()) {
            return PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        int pageNumber = Math.max(pageable.getPageNumber(), 0);
        int pageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "createdAt");
        return PageRequest.of(pageNumber, pageSize, sort);
    }

    private String normalizeSlug(String slug) {
        return SlugUtils.normalize(slug);
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private ApiDetailResponse toDetailResponse(ApiEntity entity) {
        ApiDetailResponse response = apiCatalogMapper.toDetailResponse(entity);
        Double average = reviewRepository.calculateAverageRating(entity.getId());
        response.setRatingAverage(average == null ? 0.0 : Math.round(average * 100.0) / 100.0);
        response.setRatingCount(reviewRepository.calculateReviewCount(entity.getId()));
        response.setOneStar(reviewRepository.countByApiIdAndRatingAndDeletedAtIsNull(entity.getId(), (short) 1));
        response.setTwoStars(reviewRepository.countByApiIdAndRatingAndDeletedAtIsNull(entity.getId(), (short) 2));
        response.setThreeStars(reviewRepository.countByApiIdAndRatingAndDeletedAtIsNull(entity.getId(), (short) 3));
        response.setFourStars(reviewRepository.countByApiIdAndRatingAndDeletedAtIsNull(entity.getId(), (short) 4));
        response.setFiveStars(reviewRepository.countByApiIdAndRatingAndDeletedAtIsNull(entity.getId(), (short) 5));
        return response;
    }
}
