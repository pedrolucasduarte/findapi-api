package com.findapi.api.collection.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.findapi.api.apiCatalog.dto.response.ApiResponse;
import com.findapi.api.apiCatalog.mapper.ApiCatalogMapper;
import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.collection.repository.CollectionApiRepository;
import com.findapi.api.collection.repository.CollectionRepository;
import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.common.exception.ResourceNotFoundException;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.entity.CollectionApiEntity;
import com.findapi.api.entity.CollectionApiId;
import com.findapi.api.entity.CollectionEntity;
import com.findapi.api.security.authorization.SecurityAuthorities;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CollectionApiService {
    private static final Logger LOGGER = LoggerFactory.getLogger(CollectionApiService.class);

    private final CollectionRepository collectionRepository;
    private final ApiRepository apiRepository;
    private final CollectionApiRepository collectionApiRepository;
    private final ApiCatalogMapper apiCatalogMapper;

    @Transactional
    public void addApi(UUID collectionId, UUID apiId) {
        CollectionEntity collection = findCollection(collectionId);
        assertOwnerOrAdmin(collection, authenticatedUserId());
        ApiEntity api = findApi(apiId);
        Optional<CollectionApiEntity> existing = collectionApiRepository.findByCollectionIdAndApiId(collectionId, apiId);
        if (existing.isPresent() && existing.get().getDeletedAt() == null) {
            throw new BusinessException("Collection API relationship already exists.");
        }
        CollectionApiEntity entity = existing.orElseGet(() -> newCollectionApi(collection, api));
        entity.setDeletedAt(null);
        collectionApiRepository.save(entity);
        LOGGER.info("audit action=CREATE entity=CollectionApi collectionId={} apiId={}", collectionId, apiId);
    }

    @Transactional
    public void removeApi(UUID collectionId, UUID apiId) {
        CollectionEntity collection = findCollection(collectionId);
        assertOwnerOrAdmin(collection, authenticatedUserId());
        CollectionApiEntity entity = collectionApiRepository
                .findByCollectionIdAndApiIdAndDeletedAtIsNull(collectionId, apiId)
                .orElseThrow(() -> new ResourceNotFoundException("Collection API relationship not found."));
        entity.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        collectionApiRepository.save(entity);
        LOGGER.info("audit action=DELETE entity=CollectionApi collectionId={} apiId={}", collectionId, apiId);
    }

    @Transactional(readOnly = true)
    public List<ApiResponse> findApis(UUID collectionId) {
        findCollection(collectionId);
        return collectionApiRepository.findByCollectionIdAndDeletedAtIsNull(collectionId)
                .stream()
                .map(CollectionApiEntity::getApi)
                .map(apiCatalogMapper::toResponse)
                .toList();
    }

    private CollectionApiEntity newCollectionApi(CollectionEntity collection, ApiEntity api) {
        CollectionApiEntity entity = new CollectionApiEntity();
        entity.setId(new CollectionApiId(collection.getId(), api.getId()));
        entity.setCollection(collection);
        entity.setApi(api);
        return entity;
    }

    private CollectionEntity findCollection(UUID id) {
        return collectionRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Collection not found."));
    }

    private ApiEntity findApi(UUID id) {
        return apiRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("API not found."));
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
            throw new BusinessException("Authenticated user is required.");
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
}
