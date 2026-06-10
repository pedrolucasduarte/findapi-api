package com.findapi.api.tag.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.common.exception.ResourceNotFoundException;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.entity.TagEntity;
import com.findapi.api.tag.dto.request.TagCreateRequest;
import com.findapi.api.tag.dto.request.TagFilterRequest;
import com.findapi.api.tag.dto.request.TagUpdateRequest;
import com.findapi.api.tag.dto.response.TagDetailResponse;
import com.findapi.api.tag.dto.response.TagResponse;
import com.findapi.api.tag.mapper.TagMapper;
import com.findapi.api.tag.repository.TagRepository;

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
class TagServiceTest {
    private static final UUID TAG_ID = UUID.fromString("d4fb92b8-b53c-4f25-b089-9e7d433c7f4d");

    @Mock
    private TagRepository tagRepository;

    @Mock
    private TagMapper tagMapper;

    private TagService tagService;

    @BeforeEach
    void setUp() {
        tagService = new TagService(tagRepository, tagMapper);
    }

    @Test
    void createTagSuccessfully() {
        TagCreateRequest request = createRequest("Spring-Boot");
        TagDetailResponse expected = detailResponse("spring-boot");

        when(tagRepository.existsBySlugAndDeletedAtIsNull("spring-boot")).thenReturn(false);
        when(tagRepository.save(any(TagEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(tagMapper.entityToDetailResponse(any(TagEntity.class))).thenReturn(expected);

        TagDetailResponse response = tagService.create(request);

        ArgumentCaptor<TagEntity> captor = ArgumentCaptor.forClass(TagEntity.class);
        verify(tagRepository).save(captor.capture());
        assertThat(response).isEqualTo(expected);
        assertThat(captor.getValue().getName()).isEqualTo("Spring Boot");
        assertThat(captor.getValue().getSlug()).isEqualTo("spring-boot");
    }

    @Test
    void blockDuplicatedSlugOnCreate() {
        TagCreateRequest request = createRequest("spring-boot");
        when(tagRepository.existsBySlugAndDeletedAtIsNull("spring-boot")).thenReturn(true);

        assertThatThrownBy(() -> tagService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Tag slug already exists.");
    }

    @Test
    void findTagByIdSuccessfully() {
        TagEntity entity = activeEntity("spring-boot");
        TagDetailResponse expected = detailResponse("spring-boot");

        when(tagRepository.findByIdAndDeletedAtIsNull(TAG_ID)).thenReturn(Optional.of(entity));
        when(tagMapper.entityToDetailResponse(entity)).thenReturn(expected);

        assertThat(tagService.findById(TAG_ID)).isEqualTo(expected);
    }

    @Test
    void returnNotFoundWhenTagIdDoesNotExist() {
        when(tagRepository.findByIdAndDeletedAtIsNull(TAG_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> tagService.findById(TAG_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Tag not found.");
    }

    @Test
    void findTagBySlugSuccessfully() {
        TagEntity entity = activeEntity("spring-boot");
        TagDetailResponse expected = detailResponse("spring-boot");

        when(tagRepository.findBySlugAndDeletedAtIsNull("spring-boot")).thenReturn(Optional.of(entity));
        when(tagMapper.entityToDetailResponse(entity)).thenReturn(expected);

        assertThat(tagService.findBySlug("Spring-Boot")).isEqualTo(expected);
    }

    @Test
    void listTagsWithFilters() {
        TagEntity entity = activeEntity("spring-boot");
        TagResponse mapped = response("spring-boot");
        TagFilterRequest filter = TagFilterRequest.builder().name("spring").build();
        PageRequest request = PageRequest.of(0, 200, Sort.unsorted());

        when(tagRepository.findAll(
                ArgumentMatchers.<Specification<TagEntity>>any(),
                any(PageRequest.class)))
                .thenReturn(new PageImpl<>(
                        List.of(entity),
                        PageRequest.of(0, 100, Sort.by("createdAt").descending()),
                        1
                ));
        when(tagMapper.entityToResponse(entity)).thenReturn(mapped);

        PageResponse<TagResponse> page = tagService.findAll(filter, request);

        assertThat(page.content()).containsExactly(mapped);
        assertThat(page.size()).isEqualTo(100);
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void updateTagSuccessfully() {
        TagEntity entity = activeEntity("old-tag");
        TagUpdateRequest request = updateRequest("New-Tag");
        TagDetailResponse expected = detailResponse("new-tag");

        when(tagRepository.findByIdAndDeletedAtIsNull(TAG_ID)).thenReturn(Optional.of(entity));
        when(tagRepository.existsBySlugAndIdNotAndDeletedAtIsNull("new-tag", TAG_ID)).thenReturn(false);
        when(tagRepository.save(entity)).thenReturn(entity);
        when(tagMapper.entityToDetailResponse(entity)).thenReturn(expected);

        TagDetailResponse response = tagService.update(TAG_ID, request);

        assertThat(response).isEqualTo(expected);
        assertThat(entity.getName()).isEqualTo("New Tag");
        assertThat(entity.getSlug()).isEqualTo("new-tag");
    }

    @Test
    void blockDuplicatedSlugOnUpdate() {
        TagEntity entity = activeEntity("old-tag");
        TagUpdateRequest request = updateRequest("Existing-Tag");

        when(tagRepository.findByIdAndDeletedAtIsNull(TAG_ID)).thenReturn(Optional.of(entity));
        when(tagRepository.existsBySlugAndIdNotAndDeletedAtIsNull("existing-tag", TAG_ID)).thenReturn(true);

        assertThatThrownBy(() -> tagService.update(TAG_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Tag slug already exists.");
    }

    @Test
    void removeTagWithSoftDelete() {
        TagEntity entity = activeEntity("spring-boot");
        when(tagRepository.findByIdAndDeletedAtIsNull(TAG_ID)).thenReturn(Optional.of(entity));

        tagService.delete(TAG_ID);

        assertThat(entity.getDeletedAt()).isNotNull();
        verify(tagRepository).save(entity);
    }

    private TagCreateRequest createRequest(String slug) {
        return TagCreateRequest.builder()
                .name("Spring Boot")
                .slug(slug)
                .build();
    }

    private TagUpdateRequest updateRequest(String slug) {
        return TagUpdateRequest.builder()
                .name("New Tag")
                .slug(slug)
                .build();
    }

    private TagEntity activeEntity(String slug) {
        TagEntity entity = new TagEntity();
        entity.setId(TAG_ID);
        entity.setName("Spring Boot");
        entity.setSlug(slug);
        return entity;
    }

    private TagDetailResponse detailResponse(String slug) {
        return TagDetailResponse.builder()
                .id(TAG_ID)
                .name("Spring Boot")
                .slug(slug)
                .build();
    }

    private TagResponse response(String slug) {
        return TagResponse.builder()
                .id(TAG_ID)
                .name("Spring Boot")
                .slug(slug)
                .build();
    }
}
