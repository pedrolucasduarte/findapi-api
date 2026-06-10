package com.findapi.api.collection.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.findapi.api.collection.dto.request.CollectionCreateRequest;
import com.findapi.api.collection.dto.request.CollectionFilterRequest;
import com.findapi.api.collection.dto.request.CollectionUpdateRequest;
import com.findapi.api.collection.dto.response.CollectionDetailResponse;
import com.findapi.api.collection.dto.response.CollectionResponse;
import com.findapi.api.collection.mapper.CollectionMapper;
import com.findapi.api.collection.repository.CollectionRepository;
import com.findapi.api.collection.specification.CollectionSpecification;
import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.common.exception.ResourceNotFoundException;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.common.util.SlugUtils;
import com.findapi.api.entity.AppUserEntity;
import com.findapi.api.entity.CollectionEntity;
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
public class CollectionService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String COLLECTION_NOT_FOUND = "Collection not found.";
    private static final String USER_NOT_FOUND = "Authenticated user not found.";
    private static final String AUTHENTICATED_USER_REQUIRED = "Authenticated user is required.";
    private static final String SLUG_ALREADY_EXISTS = "Collection slug already exists.";

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final CollectionMapper collectionMapper;

    @Transactional
    @CacheEvict(cacheNames = "dashboard", allEntries = true)
    public CollectionDetailResponse create(CollectionCreateRequest request) {
        UUID ownerId = authenticatedUserId();
        AppUserEntity owner = findActiveUser(ownerId);
        String normalizedSlug = SlugUtils.normalize(request.getSlug());

        if (collectionRepository.existsBySlugAndDeletedAtIsNull(normalizedSlug)) {
            throw new BusinessException(SLUG_ALREADY_EXISTS);
        }

        CollectionEntity entity = new CollectionEntity();
        entity.setUser(owner);
        applyRequest(entity, request.getName(), normalizedSlug, request.getDescription());
        return collectionMapper.entityToDetailResponse(collectionRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public CollectionDetailResponse findById(UUID id) {
        return collectionMapper.entityToDetailResponse(findActiveEntity(id));
    }

    @Transactional(readOnly = true)
    public CollectionDetailResponse findBySlug(String slug) {
        String normalizedSlug = SlugUtils.normalize(slug);
        CollectionEntity entity = collectionRepository.findBySlugAndDeletedAtIsNull(normalizedSlug)
                .orElseThrow(() -> new ResourceNotFoundException(COLLECTION_NOT_FOUND));
        return collectionMapper.entityToDetailResponse(entity);
    }

    @Transactional(readOnly = true)
    public PageResponse<CollectionResponse> findAll(CollectionFilterRequest filter, Pageable pageable) {
        Pageable safePageable = safePageable(pageable);
        return PageResponse.from(collectionRepository.findAll(CollectionSpecification.fromFilter(filter), safePageable)
                .map(collectionMapper::entityToResponse));
    }

    @Transactional
    @CacheEvict(cacheNames = "dashboard", allEntries = true)
    public CollectionDetailResponse update(UUID id, CollectionUpdateRequest request) {
        CollectionEntity entity = findActiveEntity(id);
        assertOwnerOrAdmin(entity, authenticatedUserId());
        String normalizedSlug = SlugUtils.normalize(request.getSlug());

        if (collectionRepository.existsBySlugAndIdNotAndDeletedAtIsNull(normalizedSlug, id)) {
            throw new BusinessException(SLUG_ALREADY_EXISTS);
        }

        applyRequest(entity, request.getName(), normalizedSlug, request.getDescription());
        return collectionMapper.entityToDetailResponse(collectionRepository.save(entity));
    }

    @Transactional
    @CacheEvict(cacheNames = "dashboard", allEntries = true)
    public void delete(UUID id) {
        CollectionEntity entity = findActiveEntity(id);
        assertOwnerOrAdmin(entity, authenticatedUserId());
        entity.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        collectionRepository.save(entity);
    }

    private void applyRequest(CollectionEntity entity, String name, String slug, String description) {
        entity.setName(clean(name));
        entity.setSlug(slug);
        entity.setDescription(clean(description));
    }

    private AppUserEntity findActiveUser(UUID id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
    }

    private CollectionEntity findActiveEntity(UUID id) {
        return collectionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(COLLECTION_NOT_FOUND));
    }

    private void assertOwnerOrAdmin(CollectionEntity entity, UUID userId) {
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
