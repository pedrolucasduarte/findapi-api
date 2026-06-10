package com.findapi.api.tag.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.common.exception.ResourceNotFoundException;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.common.util.SlugUtils;
import com.findapi.api.entity.TagEntity;
import com.findapi.api.tag.dto.request.TagCreateRequest;
import com.findapi.api.tag.dto.request.TagFilterRequest;
import com.findapi.api.tag.dto.request.TagUpdateRequest;
import com.findapi.api.tag.dto.response.TagDetailResponse;
import com.findapi.api.tag.dto.response.TagResponse;
import com.findapi.api.tag.mapper.TagMapper;
import com.findapi.api.tag.repository.TagRepository;
import com.findapi.api.tag.specification.TagSpecification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TagService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String TAG_NOT_FOUND = "Tag not found.";
    private static final String SLUG_ALREADY_EXISTS = "Tag slug already exists.";

    private final TagRepository tagRepository;
    private final TagMapper tagMapper;

    @Transactional
    @CacheEvict(cacheNames = "dashboard", allEntries = true)
    public TagDetailResponse create(TagCreateRequest request) {
        String normalizedSlug = normalizeSlug(request.getSlug());
        assertSlugAvailable(normalizedSlug);

        TagEntity entity = new TagEntity();
        entity.setName(clean(request.getName()));
        entity.setSlug(normalizedSlug);

        return tagMapper.entityToDetailResponse(tagRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public TagDetailResponse findById(UUID id) {
        return tagMapper.entityToDetailResponse(findActiveEntity(id));
    }

    @Transactional(readOnly = true)
    public TagDetailResponse findBySlug(String slug) {
        return tagRepository.findBySlugAndDeletedAtIsNull(normalizeSlug(slug))
                .map(tagMapper::entityToDetailResponse)
                .orElseThrow(() -> new ResourceNotFoundException(TAG_NOT_FOUND));
    }

    @Transactional(readOnly = true)
    public PageResponse<TagResponse> findAll(TagFilterRequest filter, Pageable pageable) {
        Pageable safePageable = safePageable(pageable);
        return PageResponse.from(tagRepository.findAll(TagSpecification.fromFilter(filter), safePageable)
                .map(tagMapper::entityToResponse));
    }

    @Transactional
    @CacheEvict(cacheNames = "dashboard", allEntries = true)
    public TagDetailResponse update(UUID id, TagUpdateRequest request) {
        TagEntity entity = findActiveEntity(id);
        String normalizedSlug = normalizeSlug(request.getSlug());

        if (tagRepository.existsBySlugAndIdNotAndDeletedAtIsNull(normalizedSlug, id)) {
            throw new BusinessException(SLUG_ALREADY_EXISTS);
        }

        entity.setName(clean(request.getName()));
        entity.setSlug(normalizedSlug);
        return tagMapper.entityToDetailResponse(tagRepository.save(entity));
    }

    @Transactional
    @CacheEvict(cacheNames = "dashboard", allEntries = true)
    public void delete(UUID id) {
        TagEntity entity = findActiveEntity(id);
        entity.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        tagRepository.save(entity);
    }

    private TagEntity findActiveEntity(UUID id) {
        return tagRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(TAG_NOT_FOUND));
    }

    private void assertSlugAvailable(String slug) {
        if (tagRepository.existsBySlugAndDeletedAtIsNull(slug)) {
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
