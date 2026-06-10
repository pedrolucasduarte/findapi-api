package com.findapi.api.user.service;

import java.util.UUID;

import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.common.exception.ResourceNotFoundException;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.entity.AppUserEntity;
import com.findapi.api.user.dto.request.UserFilterRequest;
import com.findapi.api.user.dto.request.UserUpdateRequest;
import com.findapi.api.user.dto.response.UserResponse;
import com.findapi.api.user.mapper.UserMapper;
import com.findapi.api.user.repository.UserRepository;
import com.findapi.api.user.specification.UserSpecification;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserService.class);
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String USER_NOT_FOUND = "User not found.";
    private static final String AUTHENTICATED_USER_REQUIRED = "Authenticated user is required.";

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Transactional(readOnly = true)
    public UserResponse profile() {
        return userMapper.entityToResponse(findActiveEntity(authenticatedUserId()));
    }

    @Transactional
    public UserResponse updateProfile(UserUpdateRequest request) {
        UUID userId = authenticatedUserId();
        AppUserEntity entity = findActiveEntity(userId);
        String email = clean(request.getEmail()).toLowerCase(java.util.Locale.ROOT);

        if (userRepository.existsByEmailIgnoreCaseAndIdNotAndDeletedAtIsNull(email, userId)) {
            throw new BusinessException("User email already exists.");
        }

        entity.setName(clean(request.getName()));
        entity.setEmail(email);
        LOGGER.info("audit action=UPDATE entity=AppUser id={}", entity.getId());
        return userMapper.entityToResponse(userRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public UserResponse findById(UUID id) {
        return userMapper.entityToResponse(findActiveEntity(id));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> findAll(UserFilterRequest filter, Pageable pageable) {
        Pageable safePageable = safePageable(pageable);
        return PageResponse.from(userRepository.findAll(UserSpecification.fromFilter(filter), safePageable)
                .map(userMapper::entityToResponse));
    }

    private AppUserEntity findActiveEntity(UUID id) {
        return userRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(USER_NOT_FOUND));
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

    private Pageable safePageable(Pageable pageable) {
        if (pageable.isUnpaged()) {
            return PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        }
        int pageNumber = Math.max(pageable.getPageNumber(), 0);
        int pageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Sort sort = pageable.getSort().isSorted() ? pageable.getSort() : Sort.by(Sort.Direction.DESC, "createdAt");
        return PageRequest.of(pageNumber, pageSize, sort);
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }
}
