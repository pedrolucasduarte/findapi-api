package com.findapi.api.apiCatalog.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;

import com.findapi.api.apiCatalog.repository.ApiCategoryRepository;
import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.apiCatalog.repository.ApiTagRepository;
import com.findapi.api.category.mapper.CategoryMapper;
import com.findapi.api.category.repository.CategoryRepository;
import com.findapi.api.category.dto.response.CategoryResponse;
import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.common.exception.ResourceNotFoundException;
import com.findapi.api.entity.ApiCategoryEntity;
import com.findapi.api.entity.ApiCategoryId;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.entity.ApiTagEntity;
import com.findapi.api.entity.ApiTagId;
import com.findapi.api.entity.CategoryEntity;
import com.findapi.api.entity.TagEntity;
import com.findapi.api.tag.dto.response.TagResponse;
import com.findapi.api.tag.mapper.TagMapper;
import com.findapi.api.tag.repository.TagRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ApiRelationshipService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiRelationshipService.class);

    private final ApiRepository apiRepository;
    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final ApiCategoryRepository apiCategoryRepository;
    private final ApiTagRepository apiTagRepository;
    private final CategoryMapper categoryMapper;
    private final TagMapper tagMapper;

    @Transactional
    public void addCategory(UUID apiId, UUID categoryId) {
        ApiEntity api = findApi(apiId);
        CategoryEntity category = findCategory(categoryId);
        java.util.Optional<ApiCategoryEntity> existing = apiCategoryRepository.findByApiIdAndCategoryId(apiId, categoryId);
        if (existing.isPresent() && existing.get().getDeletedAt() == null) {
            throw new BusinessException("API category relationship already exists.");
        }
        ApiCategoryEntity entity = existing.orElseGet(() -> newApiCategory(api, category));
        entity.setDeletedAt(null);
        apiCategoryRepository.save(entity);
        LOGGER.info("audit action=CREATE entity=ApiCategory apiId={} categoryId={}", apiId, categoryId);
    }

    @Transactional
    public void removeCategory(UUID apiId, UUID categoryId) {
        ApiCategoryEntity entity = apiCategoryRepository.findByApiIdAndCategoryIdAndDeletedAtIsNull(apiId, categoryId)
                .orElseThrow(() -> new ResourceNotFoundException("API category relationship not found."));
        entity.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        apiCategoryRepository.save(entity);
        LOGGER.info("audit action=DELETE entity=ApiCategory apiId={} categoryId={}", apiId, categoryId);
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findCategories(UUID apiId) {
        findApi(apiId);
        return apiCategoryRepository.findByApiIdAndDeletedAtIsNull(apiId)
                .stream()
                .map(ApiCategoryEntity::getCategory)
                .map(categoryMapper::entityToResponse)
                .toList();
    }

    @Transactional
    public void addTag(UUID apiId, UUID tagId) {
        ApiEntity api = findApi(apiId);
        TagEntity tag = findTag(tagId);
        java.util.Optional<ApiTagEntity> existing = apiTagRepository.findByApiIdAndTagId(apiId, tagId);
        if (existing.isPresent() && existing.get().getDeletedAt() == null) {
            throw new BusinessException("API tag relationship already exists.");
        }
        ApiTagEntity entity = existing.orElseGet(() -> newApiTag(api, tag));
        entity.setDeletedAt(null);
        apiTagRepository.save(entity);
        LOGGER.info("audit action=CREATE entity=ApiTag apiId={} tagId={}", apiId, tagId);
    }

    @Transactional
    public void removeTag(UUID apiId, UUID tagId) {
        ApiTagEntity entity = apiTagRepository.findByApiIdAndTagIdAndDeletedAtIsNull(apiId, tagId)
                .orElseThrow(() -> new ResourceNotFoundException("API tag relationship not found."));
        entity.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        apiTagRepository.save(entity);
        LOGGER.info("audit action=DELETE entity=ApiTag apiId={} tagId={}", apiId, tagId);
    }

    @Transactional(readOnly = true)
    public List<TagResponse> findTags(UUID apiId) {
        findApi(apiId);
        return apiTagRepository.findByApiIdAndDeletedAtIsNull(apiId)
                .stream()
                .map(ApiTagEntity::getTag)
                .map(tagMapper::entityToResponse)
                .toList();
    }

    private ApiCategoryEntity newApiCategory(ApiEntity api, CategoryEntity category) {
        ApiCategoryEntity entity = new ApiCategoryEntity();
        entity.setId(new ApiCategoryId(api.getId(), category.getId()));
        entity.setApi(api);
        entity.setCategory(category);
        return entity;
    }

    private ApiTagEntity newApiTag(ApiEntity api, TagEntity tag) {
        ApiTagEntity entity = new ApiTagEntity();
        entity.setId(new ApiTagId(api.getId(), tag.getId()));
        entity.setApi(api);
        entity.setTag(tag);
        return entity;
    }

    private ApiEntity findApi(UUID id) {
        return apiRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("API not found."));
    }

    private CategoryEntity findCategory(UUID id) {
        return categoryRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Category not found."));
    }

    private TagEntity findTag(UUID id) {
        return tagRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException("Tag not found."));
    }
}
