package com.findapi.api.collection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.findapi.api.collection.dto.request.CollectionCreateRequest;
import com.findapi.api.collection.dto.request.CollectionFilterRequest;
import com.findapi.api.collection.dto.request.CollectionUpdateRequest;
import com.findapi.api.collection.dto.response.CollectionDetailResponse;
import com.findapi.api.collection.dto.response.CollectionResponse;
import com.findapi.api.collection.mapper.CollectionMapper;
import com.findapi.api.collection.repository.CollectionRepository;
import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.common.exception.ResourceNotFoundException;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.entity.AppUserEntity;
import com.findapi.api.entity.CollectionEntity;
import com.findapi.api.enums.UserRole;
import com.findapi.api.user.repository.UserRepository;

import org.junit.jupiter.api.AfterEach;
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
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {
    private static final UUID COLLECTION_ID = UUID.fromString("1e132356-95b6-4e18-85e7-6f9a326342b1");
    private static final UUID OWNER_ID = UUID.fromString("a9ef0f06-d716-4419-b7a9-58c820cc4894");
    private static final UUID OTHER_USER_ID = UUID.fromString("bb275dbe-65e0-475c-b714-f9b8d5ff9db6");

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CollectionMapper collectionMapper;

    private CollectionService collectionService;

    @BeforeEach
    void setUp() {
        collectionService = new CollectionService(collectionRepository, userRepository, collectionMapper);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void createCollectionSuccessfully() {
        authenticate(OWNER_ID, "ROLE_USER");
        CollectionCreateRequest request = createRequest(" Dev Tools ", " DEV-TOOLS ");
        CollectionDetailResponse expected = detailResponse("Dev Tools", "dev-tools", OWNER_ID);

        when(userRepository.findByIdAndDeletedAtIsNull(OWNER_ID)).thenReturn(Optional.of(userEntity(OWNER_ID)));
        when(collectionRepository.existsBySlugAndDeletedAtIsNull("dev-tools")).thenReturn(false);
        when(collectionRepository.save(any(CollectionEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(collectionMapper.entityToDetailResponse(any(CollectionEntity.class))).thenReturn(expected);

        CollectionDetailResponse response = collectionService.create(request);

        ArgumentCaptor<CollectionEntity> captor = ArgumentCaptor.forClass(CollectionEntity.class);
        verify(collectionRepository).save(captor.capture());
        assertThat(response).isEqualTo(expected);
        assertThat(captor.getValue().getUser().getId()).isEqualTo(OWNER_ID);
        assertThat(captor.getValue().getName()).isEqualTo("Dev Tools");
        assertThat(captor.getValue().getSlug()).isEqualTo("dev-tools");
    }

    @Test
    void blockDuplicatedSlug() {
        authenticate(OWNER_ID, "ROLE_USER");
        when(userRepository.findByIdAndDeletedAtIsNull(OWNER_ID)).thenReturn(Optional.of(userEntity(OWNER_ID)));
        when(collectionRepository.existsBySlugAndDeletedAtIsNull("dev-tools")).thenReturn(true);

        assertThatThrownBy(() -> collectionService.create(createRequest("Dev Tools", "dev-tools")))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Collection slug already exists.");
    }

    @Test
    void findCollectionByIdSuccessfully() {
        CollectionEntity entity = activeEntity(OWNER_ID, "Dev Tools", "dev-tools");
        CollectionDetailResponse expected = detailResponse("Dev Tools", "dev-tools", OWNER_ID);

        when(collectionRepository.findByIdAndDeletedAtIsNull(COLLECTION_ID)).thenReturn(Optional.of(entity));
        when(collectionMapper.entityToDetailResponse(entity)).thenReturn(expected);

        assertThat(collectionService.findById(COLLECTION_ID)).isEqualTo(expected);
    }

    @Test
    void findCollectionBySlugSuccessfully() {
        CollectionEntity entity = activeEntity(OWNER_ID, "Dev Tools", "dev-tools");
        CollectionDetailResponse expected = detailResponse("Dev Tools", "dev-tools", OWNER_ID);

        when(collectionRepository.findBySlugAndDeletedAtIsNull("dev-tools")).thenReturn(Optional.of(entity));
        when(collectionMapper.entityToDetailResponse(entity)).thenReturn(expected);

        assertThat(collectionService.findBySlug(" DEV-TOOLS ")).isEqualTo(expected);
    }

    @Test
    void listCollectionsWithFilters() {
        CollectionEntity entity = activeEntity(OWNER_ID, "Dev Tools", "dev-tools");
        CollectionResponse mapped = response("Dev Tools", "dev-tools", OWNER_ID);
        CollectionFilterRequest filter = CollectionFilterRequest.builder()
                .name("dev")
                .slug("DEV-TOOLS")
                .ownerId(OWNER_ID)
                .build();
        PageRequest request = PageRequest.of(0, 200, Sort.unsorted());

        when(collectionRepository.findAll(
                ArgumentMatchers.<Specification<CollectionEntity>>any(),
                any(PageRequest.class)))
                .thenReturn(new PageImpl<>(
                        List.of(entity),
                        PageRequest.of(0, 100, Sort.by("createdAt").descending()),
                        1
                ));
        when(collectionMapper.entityToResponse(entity)).thenReturn(mapped);

        PageResponse<CollectionResponse> page = collectionService.findAll(filter, request);

        assertThat(page.content()).containsExactly(mapped);
        assertThat(page.size()).isEqualTo(100);
    }

    @Test
    void updateCollectionAsOwner() {
        authenticate(OWNER_ID, "ROLE_USER");
        CollectionEntity entity = activeEntity(OWNER_ID, "Dev Tools", "dev-tools");
        CollectionUpdateRequest request = updateRequest("Backend APIs", "backend-apis");
        CollectionDetailResponse expected = detailResponse("Backend APIs", "backend-apis", OWNER_ID);

        when(collectionRepository.findByIdAndDeletedAtIsNull(COLLECTION_ID)).thenReturn(Optional.of(entity));
        when(collectionRepository.existsBySlugAndIdNotAndDeletedAtIsNull("backend-apis", COLLECTION_ID))
                .thenReturn(false);
        when(collectionRepository.save(entity)).thenReturn(entity);
        when(collectionMapper.entityToDetailResponse(entity)).thenReturn(expected);

        CollectionDetailResponse response = collectionService.update(COLLECTION_ID, request);

        assertThat(response).isEqualTo(expected);
        assertThat(entity.getName()).isEqualTo("Backend APIs");
        assertThat(entity.getSlug()).isEqualTo("backend-apis");
    }

    @Test
    void blockUpdateByAnotherUser() {
        authenticate(OTHER_USER_ID, "ROLE_USER");
        when(collectionRepository.findByIdAndDeletedAtIsNull(COLLECTION_ID))
                .thenReturn(Optional.of(activeEntity(OWNER_ID, "Dev Tools", "dev-tools")));

        assertThatThrownBy(() -> collectionService.update(COLLECTION_ID, updateRequest("Backend APIs", "backend-apis")))
                .isInstanceOf(AuthorizationDeniedException.class)
                .hasMessage("Access denied.");
    }

    @Test
    void deleteCollectionAsOwner() {
        authenticate(OWNER_ID, "ROLE_USER");
        CollectionEntity entity = activeEntity(OWNER_ID, "Dev Tools", "dev-tools");
        when(collectionRepository.findByIdAndDeletedAtIsNull(COLLECTION_ID)).thenReturn(Optional.of(entity));

        collectionService.delete(COLLECTION_ID);

        assertThat(entity.getDeletedAt()).isNotNull();
        verify(collectionRepository).save(entity);
    }

    @Test
    void blockDeleteByAnotherUser() {
        authenticate(OTHER_USER_ID, "ROLE_USER");
        when(collectionRepository.findByIdAndDeletedAtIsNull(COLLECTION_ID))
                .thenReturn(Optional.of(activeEntity(OWNER_ID, "Dev Tools", "dev-tools")));

        assertThatThrownBy(() -> collectionService.delete(COLLECTION_ID))
                .isInstanceOf(AuthorizationDeniedException.class)
                .hasMessage("Access denied.");
    }

    @Test
    void returnNotFoundWhenCollectionIdDoesNotExist() {
        when(collectionRepository.findByIdAndDeletedAtIsNull(COLLECTION_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> collectionService.findById(COLLECTION_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Collection not found.");
    }

    private void authenticate(UUID userId, String authority) {
        TestingAuthenticationToken authentication = new TestingAuthenticationToken(userId.toString(), "token", authority);
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private CollectionCreateRequest createRequest(String name, String slug) {
        return CollectionCreateRequest.builder()
                .name(name)
                .slug(slug)
                .description("Useful APIs")
                .build();
    }

    private CollectionUpdateRequest updateRequest(String name, String slug) {
        return CollectionUpdateRequest.builder()
                .name(name)
                .slug(slug)
                .description("Updated collection")
                .build();
    }

    private CollectionEntity activeEntity(UUID ownerId, String name, String slug) {
        CollectionEntity entity = new CollectionEntity();
        entity.setId(COLLECTION_ID);
        entity.setUser(userEntity(ownerId));
        entity.setName(name);
        entity.setSlug(slug);
        entity.setDescription("Useful APIs");
        return entity;
    }

    private AppUserEntity userEntity(UUID userId) {
        AppUserEntity user = new AppUserEntity();
        user.setId(userId);
        user.setName("Owner");
        user.setEmail("%s@example.com".formatted(userId));
        user.setPasswordHash("hash");
        user.setRole(UserRole.USER);
        return user;
    }

    private CollectionDetailResponse detailResponse(String name, String slug, UUID ownerId) {
        return CollectionDetailResponse.builder()
                .id(COLLECTION_ID)
                .ownerId(ownerId)
                .name(name)
                .slug(slug)
                .description("Useful APIs")
                .build();
    }

    private CollectionResponse response(String name, String slug, UUID ownerId) {
        return CollectionResponse.builder()
                .id(COLLECTION_ID)
                .ownerId(ownerId)
                .name(name)
                .slug(slug)
                .description("Useful APIs")
                .build();
    }
}
