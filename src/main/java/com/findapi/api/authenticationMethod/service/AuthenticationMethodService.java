package com.findapi.api.authenticationMethod.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import com.findapi.api.authenticationMethod.dto.request.AuthenticationMethodCreateRequest;
import com.findapi.api.authenticationMethod.dto.request.AuthenticationMethodFilterRequest;
import com.findapi.api.authenticationMethod.dto.request.AuthenticationMethodUpdateRequest;
import com.findapi.api.authenticationMethod.dto.response.AuthenticationMethodDetailResponse;
import com.findapi.api.authenticationMethod.dto.response.AuthenticationMethodResponse;
import com.findapi.api.authenticationMethod.mapper.AuthenticationMethodMapper;
import com.findapi.api.authenticationMethod.repository.AuthenticationMethodRepository;
import com.findapi.api.authenticationMethod.specification.AuthenticationMethodSpecification;
import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.common.exception.ResourceNotFoundException;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.entity.AuthenticationMethodEntity;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthenticationMethodService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String METHOD_NOT_FOUND = "Authentication method not found.";
    private static final String NAME_ALREADY_EXISTS = "Authentication method name already exists.";
    private static final String METHOD_IN_USE = "Authentication method is in use by active APIs.";
    private static final String INVALID_NAME = "Authentication method name is not supported by the current schema.";
    private static final Set<String> ALLOWED_NAMES = Set.of(
            "NONE",
            "API_KEY",
            "BEARER",
            "OAUTH2",
            "BASIC_AUTH",
            "HMAC"
    );

    private final AuthenticationMethodRepository authenticationMethodRepository;
    private final AuthenticationMethodMapper authenticationMethodMapper;
    private final EntityManager entityManager;

    @Transactional
    public AuthenticationMethodDetailResponse create(AuthenticationMethodCreateRequest request) {
        String normalizedName = normalizeAndValidateName(request.getName());
        assertNameAvailable(normalizedName);

        AuthenticationMethodEntity entity = new AuthenticationMethodEntity();
        entity.setName(normalizedName);

        return authenticationMethodMapper.entityToDetailResponse(authenticationMethodRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public AuthenticationMethodDetailResponse findById(UUID id) {
        return authenticationMethodMapper.entityToDetailResponse(findActiveEntity(id));
    }

    @Transactional(readOnly = true)
    public AuthenticationMethodDetailResponse findByName(String name) {
        return authenticationMethodRepository.findByNameAndDeletedAtIsNull(normalizeAndValidateName(name))
                .map(authenticationMethodMapper::entityToDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException(METHOD_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PageResponse<AuthenticationMethodResponse> findAll(
            AuthenticationMethodFilterRequest filter,
            Pageable pageable
    ) {
        Pageable safePageable = safePageable(pageable);
        return PageResponse.from(authenticationMethodRepository
                .findAll(AuthenticationMethodSpecification.fromFilter(filter), safePageable)
                .map(authenticationMethodMapper::entityToResponse));
    }

    @Transactional
    public AuthenticationMethodDetailResponse update(UUID id, AuthenticationMethodUpdateRequest request) {
        AuthenticationMethodEntity entity = findActiveEntity(id);
        String normalizedName = normalizeAndValidateName(request.getName());

        if (authenticationMethodRepository.existsByNameAndIdNotAndDeletedAtIsNull(normalizedName, id)) {
            throw new BusinessException(NAME_ALREADY_EXISTS);
        }

        entity.setName(normalizedName);
        return authenticationMethodMapper.entityToDetailResponse(authenticationMethodRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        AuthenticationMethodEntity entity = findActiveEntity(id);
        if (isUsedByActiveApis(id)) {
            throw new BusinessException(METHOD_IN_USE);
        }

        entity.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        authenticationMethodRepository.save(entity);
    }

    private AuthenticationMethodEntity findActiveEntity(UUID id) {
        return authenticationMethodRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(METHOD_NOT_FOUND));
    }

    private void assertNameAvailable(String name) {
        if (authenticationMethodRepository.existsByNameAndDeletedAtIsNull(name)) {
            throw new BusinessException(NAME_ALREADY_EXISTS);
        }
    }

    private boolean isUsedByActiveApis(UUID id) {
        Long activeApis = entityManager
                .createQuery("""
                        select count(api)
                        from ApiEntity api
                        where api.authenticationMethod.id = :id
                          and api.deletedAt is null
                        """, Long.class)
                .setParameter("id", id)
                .getSingleResult();
        return activeApis > 0;
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

    private String normalizeAndValidateName(String name) {
        String normalizedName = name == null ? null : name.trim().toUpperCase(Locale.ROOT);
        if (!ALLOWED_NAMES.contains(normalizedName)) {
            throw new BusinessException(INVALID_NAME);
        }
        return normalizedName;
    }
}
