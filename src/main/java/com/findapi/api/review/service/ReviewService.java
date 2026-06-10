package com.findapi.api.review.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.common.exception.ResourceNotFoundException;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.entity.AppUserEntity;
import com.findapi.api.entity.ReviewEntity;
import com.findapi.api.review.dto.request.ReviewCreateRequest;
import com.findapi.api.review.dto.request.ReviewFilterRequest;
import com.findapi.api.review.dto.request.ReviewUpdateRequest;
import com.findapi.api.review.dto.response.ReviewDetailResponse;
import com.findapi.api.review.dto.response.ReviewResponse;
import com.findapi.api.review.mapper.ReviewMapper;
import com.findapi.api.review.repository.ReviewRepository;
import com.findapi.api.review.specification.ReviewSpecification;
import com.findapi.api.security.authorization.SecurityAuthorities;
import com.findapi.api.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String REVIEW_NOT_FOUND = "Review not found.";
    private static final String API_NOT_FOUND = "API not found.";
    private static final String USER_NOT_FOUND = "Authenticated user not found.";
    private static final String AUTHENTICATED_USER_REQUIRED = "Authenticated user is required.";
    private static final String DUPLICATED_REVIEW = "User has already reviewed this API.";

    private final ReviewRepository reviewRepository;
    private final ApiRepository apiRepository;
    private final UserRepository userRepository;
    private final ReviewMapper reviewMapper;

    @Transactional
    @CacheEvict(cacheNames = {"dashboard", "rankings"}, allEntries = true)
    public ReviewDetailResponse create(UUID apiId, ReviewCreateRequest request) {
        UUID userId = authenticatedUserId();
        ApiEntity api = findActiveApi(apiId);
        AppUserEntity user = findActiveUser(userId);

        if (reviewRepository.existsByApiIdAndUserIdAndDeletedAtIsNull(apiId, userId)) {
            throw new BusinessException(DUPLICATED_REVIEW);
        }

        ReviewEntity entity = new ReviewEntity();
        entity.setApi(api);
        entity.setUser(user);
        applyRequest(entity, request.getRating(), request.getComment());
        return reviewMapper.entityToDetailResponse(reviewRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public ReviewDetailResponse findById(UUID id) {
        return reviewMapper.entityToDetailResponse(findActiveEntity(id));
    }

    @Transactional(readOnly = true)
    public List<ReviewResponse> findByApiId(UUID apiId) {
        findActiveApi(apiId);
        return reviewRepository.findByApiIdAndDeletedAtIsNull(apiId)
                .stream()
                .map(reviewMapper::entityToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> findAll(ReviewFilterRequest filter, Pageable pageable) {
        Pageable safePageable = safePageable(pageable);
        return PageResponse.from(reviewRepository.findAll(ReviewSpecification.fromFilter(filter), safePageable)
                .map(reviewMapper::entityToResponse));
    }

    @Transactional
    @CacheEvict(cacheNames = {"dashboard", "rankings"}, allEntries = true)
    public ReviewDetailResponse update(UUID id, ReviewUpdateRequest request) {
        ReviewEntity entity = findActiveEntity(id);
        assertOwnerOrAdmin(entity, authenticatedUserId());
        applyRequest(entity, request.getRating(), request.getComment());
        return reviewMapper.entityToDetailResponse(reviewRepository.save(entity));
    }

    @Transactional
    @CacheEvict(cacheNames = {"dashboard", "rankings"}, allEntries = true)
    public void delete(UUID id) {
        ReviewEntity entity = findActiveEntity(id);
        assertOwnerOrAdmin(entity, authenticatedUserId());
        entity.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        reviewRepository.save(entity);
    }

    @Transactional(readOnly = true)
    public BigDecimal calculateAverageRating(UUID apiId) {
        findActiveApi(apiId);
        Double average = reviewRepository.calculateAverageRating(apiId);
        if (average == null) {
            return BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);
        }
        return BigDecimal.valueOf(average).setScale(2, RoundingMode.HALF_UP);
    }

    @Transactional(readOnly = true)
    public long calculateReviewCount(UUID apiId) {
        findActiveApi(apiId);
        return reviewRepository.calculateReviewCount(apiId);
    }

    private void applyRequest(ReviewEntity entity, Integer rating, String comment) {
        entity.setRating(rating.shortValue());
        entity.setComment(clean(comment));
    }

    private ApiEntity findActiveApi(UUID id) {
        return apiRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(API_NOT_FOUND));
    }

    private AppUserEntity findActiveUser(UUID id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }

    private ReviewEntity findActiveEntity(UUID id) {
        return reviewRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(REVIEW_NOT_FOUND));
    }

    private void assertOwnerOrAdmin(ReviewEntity entity, UUID userId) {
        if (entity.getUser().getId().equals(userId) || hasAdminAuthority()) {
            return;
        }
        throw new AuthorizationDeniedException("Access denied.");
    }

    private UUID authenticatedUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new BusinessException(AUTHENTICATED_USER_REQUIRED);
        }

        try {
            return UUID.fromString(authentication.getName());
        } catch (IllegalArgumentException exception) {
            throw new BusinessException("Authenticated user id is invalid.");
        }
    }

    private boolean hasAdminAuthority() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> SecurityAuthorities.ROLE_ADMIN.equals(authority.getAuthority()));
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

    private String clean(String value) {
        return value == null ? null : value.trim();
    }
}
