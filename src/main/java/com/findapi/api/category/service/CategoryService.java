package com.findapi.api.category.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.findapi.api.category.dto.request.CategoryCreateRequest;
import com.findapi.api.category.dto.request.CategoryFilterRequest;
import com.findapi.api.category.dto.request.CategoryUpdateRequest;
import com.findapi.api.category.dto.response.CategoryDetailResponse;
import com.findapi.api.category.dto.response.CategoryResponse;
import com.findapi.api.category.mapper.CategoryMapper;
import com.findapi.api.category.repository.CategoryRepository;
import com.findapi.api.category.specification.CategorySpecification;
import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.common.exception.ResourceNotFoundException;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.common.util.SlugUtils;
import com.findapi.api.entity.CategoryEntity;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String CATEGORY_NOT_FOUND = "Category not found.";
    private static final String SLUG_ALREADY_EXISTS = "Category slug already exists.";

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    @Transactional
    @CacheEvict(cacheNames = "dashboard", allEntries = true)
    public CategoryDetailResponse create(CategoryCreateRequest request) {
        String normalizedSlug = normalizeSlug(request.getSlug());
        assertSlugAvailable(normalizedSlug);

        CategoryEntity entity = new CategoryEntity();
        entity.setName(clean(request.getName()));
        entity.setSlug(normalizedSlug);

        return categoryMapper.entityToDetailResponse(categoryRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public CategoryDetailResponse findById(UUID id) {
        return categoryMapper.entityToDetailResponse(findActiveEntity(id));
    }

    @Transactional(readOnly = true)
    public CategoryDetailResponse findBySlug(String slug) {
        return categoryRepository.findBySlugAndDeletedAtIsNull(normalizeSlug(slug))
                .map(categoryMapper::entityToDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PageResponse<CategoryResponse> findAll(CategoryFilterRequest filter, Pageable pageable) {
        Pageable safePageable = safePageable(pageable);
        return PageResponse.from(categoryRepository.findAll(CategorySpecification.fromFilter(filter), safePageable)
                .map(categoryMapper::entityToResponse));
    }

    @Transactional
    @CacheEvict(cacheNames = "dashboard", allEntries = true)
    public CategoryDetailResponse update(UUID id, CategoryUpdateRequest request) {
        CategoryEntity entity = findActiveEntity(id);
        String normalizedSlug = normalizeSlug(request.getSlug());

        if (categoryRepository.existsBySlugAndIdNotAndDeletedAtIsNull(normalizedSlug, id)) {
            throw new BusinessException(SLUG_ALREADY_EXISTS);
        }

        entity.setName(clean(request.getName()));
        entity.setSlug(normalizedSlug);
        return categoryMapper.entityToDetailResponse(categoryRepository.save(entity));
    }

    @Transactional
    @CacheEvict(cacheNames = "dashboard", allEntries = true)
    public void delete(UUID id) {
        CategoryEntity entity = findActiveEntity(id);
        entity.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        categoryRepository.save(entity);
    }

    private CategoryEntity findActiveEntity(UUID id) {
        return categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(CATEGORY_NOT_FOUND));
    }

    private void assertSlugAvailable(String slug) {
        if (categoryRepository.existsBySlugAndDeletedAtIsNull(slug)) {
            throw new BusinessException(SLUG_ALREADY_EXISTS);
        }
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
}
