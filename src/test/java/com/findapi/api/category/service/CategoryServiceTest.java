package com.findapi.api.category.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.findapi.api.category.dto.request.CategoryCreateRequest;
import com.findapi.api.category.dto.request.CategoryFilterRequest;
import com.findapi.api.category.dto.request.CategoryUpdateRequest;
import com.findapi.api.category.dto.response.CategoryDetailResponse;
import com.findapi.api.category.dto.response.CategoryResponse;
import com.findapi.api.category.mapper.CategoryMapper;
import com.findapi.api.category.repository.CategoryRepository;
import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.common.exception.ResourceNotFoundException;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.entity.CategoryEntity;

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
class CategoryServiceTest {
    private static final UUID CATEGORY_ID = UUID.fromString("235f8d5e-23f5-4aae-bae6-f66ce573db1f");

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryService = new CategoryService(categoryRepository, categoryMapper);
    }

    @Test
    void createCategorySuccessfully() {
        CategoryCreateRequest request = createRequest("Developer-Tools");
        CategoryDetailResponse expected = detailResponse("developer-tools");

        when(categoryRepository.existsBySlugAndDeletedAtIsNull("developer-tools")).thenReturn(false);
        when(categoryRepository.save(any(CategoryEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(categoryMapper.entityToDetailResponse(any(CategoryEntity.class))).thenReturn(expected);

        CategoryDetailResponse response = categoryService.create(request);

        ArgumentCaptor<CategoryEntity> captor = ArgumentCaptor.forClass(CategoryEntity.class);
        verify(categoryRepository).save(captor.capture());
        assertThat(response).isEqualTo(expected);
        assertThat(captor.getValue().getName()).isEqualTo("Developer Tools");
        assertThat(captor.getValue().getSlug()).isEqualTo("developer-tools");
    }

    @Test
    void blockDuplicatedSlugOnCreate() {
        CategoryCreateRequest request = createRequest("developer-tools");
        when(categoryRepository.existsBySlugAndDeletedAtIsNull("developer-tools")).thenReturn(true);

        assertThatThrownBy(() -> categoryService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Category slug already exists.");
    }

    @Test
    void findCategoryByIdSuccessfully() {
        CategoryEntity entity = activeEntity("developer-tools");
        CategoryDetailResponse expected = detailResponse("developer-tools");

        when(categoryRepository.findByIdAndDeletedAtIsNull(CATEGORY_ID)).thenReturn(Optional.of(entity));
        when(categoryMapper.entityToDetailResponse(entity)).thenReturn(expected);

        assertThat(categoryService.findById(CATEGORY_ID)).isEqualTo(expected);
    }

    @Test
    void returnNotFoundWhenCategoryIdDoesNotExist() {
        when(categoryRepository.findByIdAndDeletedAtIsNull(CATEGORY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> categoryService.findById(CATEGORY_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Category not found.");
    }

    @Test
    void findCategoryBySlugSuccessfully() {
        CategoryEntity entity = activeEntity("developer-tools");
        CategoryDetailResponse expected = detailResponse("developer-tools");

        when(categoryRepository.findBySlugAndDeletedAtIsNull("developer-tools")).thenReturn(Optional.of(entity));
        when(categoryMapper.entityToDetailResponse(entity)).thenReturn(expected);

        assertThat(categoryService.findBySlug("Developer-Tools")).isEqualTo(expected);
    }

    @Test
    void listCategoriesWithFilters() {
        CategoryEntity entity = activeEntity("developer-tools");
        CategoryResponse mapped = response("developer-tools");
        CategoryFilterRequest filter = CategoryFilterRequest.builder().name("developer").build();
        PageRequest request = PageRequest.of(0, 200, Sort.unsorted());

        when(categoryRepository.findAll(
                ArgumentMatchers.<Specification<CategoryEntity>>any(),
                any(PageRequest.class)))
                .thenReturn(new PageImpl<>(
                        List.of(entity),
                        PageRequest.of(0, 100, Sort.by("createdAt").descending()),
                        1
                ));
        when(categoryMapper.entityToResponse(entity)).thenReturn(mapped);

        PageResponse<CategoryResponse> page = categoryService.findAll(filter, request);

        assertThat(page.content()).containsExactly(mapped);
        assertThat(page.size()).isEqualTo(100);
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void updateCategorySuccessfully() {
        CategoryEntity entity = activeEntity("old-category");
        CategoryUpdateRequest request = updateRequest("New-Category");
        CategoryDetailResponse expected = detailResponse("new-category");

        when(categoryRepository.findByIdAndDeletedAtIsNull(CATEGORY_ID)).thenReturn(Optional.of(entity));
        when(categoryRepository.existsBySlugAndIdNotAndDeletedAtIsNull("new-category", CATEGORY_ID))
                .thenReturn(false);
        when(categoryRepository.save(entity)).thenReturn(entity);
        when(categoryMapper.entityToDetailResponse(entity)).thenReturn(expected);

        CategoryDetailResponse response = categoryService.update(CATEGORY_ID, request);

        assertThat(response).isEqualTo(expected);
        assertThat(entity.getName()).isEqualTo("New Category");
        assertThat(entity.getSlug()).isEqualTo("new-category");
    }

    @Test
    void blockDuplicatedSlugOnUpdate() {
        CategoryEntity entity = activeEntity("old-category");
        CategoryUpdateRequest request = updateRequest("Existing-Category");

        when(categoryRepository.findByIdAndDeletedAtIsNull(CATEGORY_ID)).thenReturn(Optional.of(entity));
        when(categoryRepository.existsBySlugAndIdNotAndDeletedAtIsNull("existing-category", CATEGORY_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> categoryService.update(CATEGORY_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Category slug already exists.");
    }

    @Test
    void removeCategoryWithSoftDelete() {
        CategoryEntity entity = activeEntity("developer-tools");
        when(categoryRepository.findByIdAndDeletedAtIsNull(CATEGORY_ID)).thenReturn(Optional.of(entity));

        categoryService.delete(CATEGORY_ID);

        assertThat(entity.getDeletedAt()).isNotNull();
        verify(categoryRepository).save(entity);
    }

    private CategoryCreateRequest createRequest(String slug) {
        return CategoryCreateRequest.builder()
                .name("Developer Tools")
                .slug(slug)
                .build();
    }

    private CategoryUpdateRequest updateRequest(String slug) {
        return CategoryUpdateRequest.builder()
                .name("New Category")
                .slug(slug)
                .build();
    }

    private CategoryEntity activeEntity(String slug) {
        CategoryEntity entity = new CategoryEntity();
        entity.setId(CATEGORY_ID);
        entity.setName("Developer Tools");
        entity.setSlug(slug);
        return entity;
    }

    private CategoryDetailResponse detailResponse(String slug) {
        return CategoryDetailResponse.builder()
                .id(CATEGORY_ID)
                .name("Developer Tools")
                .slug(slug)
                .build();
    }

    private CategoryResponse response(String slug) {
        return CategoryResponse.builder()
                .id(CATEGORY_ID)
                .name("Developer Tools")
                .slug(slug)
                .build();
    }
}
