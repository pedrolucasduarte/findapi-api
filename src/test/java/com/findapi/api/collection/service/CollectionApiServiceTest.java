package com.findapi.api.collection.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.findapi.api.apiCatalog.dto.response.ApiResponse;
import com.findapi.api.apiCatalog.mapper.ApiCatalogMapper;
import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.collection.repository.CollectionApiRepository;
import com.findapi.api.collection.repository.CollectionRepository;
import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.entity.AppUserEntity;
import com.findapi.api.entity.CollectionApiEntity;
import com.findapi.api.entity.CollectionEntity;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

@ExtendWith(MockitoExtension.class)
class CollectionApiServiceTest {
    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID COLLECTION_ID = UUID.randomUUID();
    private static final UUID API_ID = UUID.randomUUID();

    @Mock private CollectionRepository collectionRepository;
    @Mock private ApiRepository apiRepository;
    @Mock private CollectionApiRepository collectionApiRepository;
    @Mock private ApiCatalogMapper apiCatalogMapper;

    private CollectionApiService service;

    @BeforeEach
    void setUp() {
        service = new CollectionApiService(
                collectionRepository,
                apiRepository,
                collectionApiRepository,
                apiCatalogMapper
        );
        SecurityContextHolder.getContext().setAuthentication(
                new TestingAuthenticationToken(USER_ID.toString(), null, "ROLE_USER")
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void addApiSuccessfully() {
        mockActiveCollectionAndApi();
        when(collectionApiRepository.findByCollectionIdAndApiId(COLLECTION_ID, API_ID))
                .thenReturn(Optional.empty());

        service.addApi(COLLECTION_ID, API_ID);

        ArgumentCaptor<CollectionApiEntity> captor = ArgumentCaptor.forClass(CollectionApiEntity.class);
        verify(collectionApiRepository).save(captor.capture());
        assertThat(captor.getValue().getId().getCollectionId()).isEqualTo(COLLECTION_ID);
        assertThat(captor.getValue().getId().getApiId()).isEqualTo(API_ID);
        assertThat(captor.getValue().getDeletedAt()).isNull();
    }

    @Test
    void rejectDuplicatedActiveRelationship() {
        mockActiveCollectionAndApi();
        when(collectionApiRepository.findByCollectionIdAndApiId(COLLECTION_ID, API_ID))
                .thenReturn(Optional.of(relationship(null)));

        assertThatThrownBy(() -> service.addApi(COLLECTION_ID, API_ID))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Collection API relationship already exists.");
    }

    @Test
    void reactivateSoftDeletedRelationship() {
        mockActiveCollectionAndApi();
        CollectionApiEntity deleted = relationship(OffsetDateTime.now());
        when(collectionApiRepository.findByCollectionIdAndApiId(COLLECTION_ID, API_ID))
                .thenReturn(Optional.of(deleted));

        service.addApi(COLLECTION_ID, API_ID);

        assertThat(deleted.getDeletedAt()).isNull();
        verify(collectionApiRepository).save(deleted);
    }

    @Test
    void softDeleteRelationship() {
        when(collectionRepository.findByIdAndDeletedAtIsNull(COLLECTION_ID))
                .thenReturn(Optional.of(collection()));
        CollectionApiEntity relationship = relationship(null);
        when(collectionApiRepository.findByCollectionIdAndApiIdAndDeletedAtIsNull(COLLECTION_ID, API_ID))
                .thenReturn(Optional.of(relationship));

        service.removeApi(COLLECTION_ID, API_ID);

        assertThat(relationship.getDeletedAt()).isNotNull();
        verify(collectionApiRepository).save(relationship);
    }

    @Test
    void listActiveApis() {
        when(collectionRepository.findByIdAndDeletedAtIsNull(COLLECTION_ID))
                .thenReturn(Optional.of(collection()));
        CollectionApiEntity relationship = relationship(null);
        ApiResponse expected = ApiResponse.builder().id(API_ID).build();
        when(collectionApiRepository.findByCollectionIdAndDeletedAtIsNull(COLLECTION_ID))
                .thenReturn(List.of(relationship));
        when(apiCatalogMapper.toResponse(relationship.getApi())).thenReturn(expected);

        assertThat(service.findApis(COLLECTION_ID)).containsExactly(expected);
    }

    private void mockActiveCollectionAndApi() {
        when(collectionRepository.findByIdAndDeletedAtIsNull(COLLECTION_ID))
                .thenReturn(Optional.of(collection()));
        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.of(api()));
    }

    private CollectionEntity collection() {
        AppUserEntity user = new AppUserEntity();
        user.setId(USER_ID);
        CollectionEntity collection = new CollectionEntity();
        collection.setId(COLLECTION_ID);
        collection.setUser(user);
        return collection;
    }

    private ApiEntity api() {
        ApiEntity api = new ApiEntity();
        api.setId(API_ID);
        return api;
    }

    private CollectionApiEntity relationship(OffsetDateTime deletedAt) {
        CollectionApiEntity relationship = new CollectionApiEntity();
        relationship.setCollection(collection());
        relationship.setApi(api());
        relationship.setDeletedAt(deletedAt);
        return relationship;
    }
}
