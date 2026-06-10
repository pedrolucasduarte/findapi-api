package com.findapi.api.authenticationMethod.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.findapi.api.authenticationMethod.dto.request.AuthenticationMethodCreateRequest;
import com.findapi.api.authenticationMethod.dto.request.AuthenticationMethodFilterRequest;
import com.findapi.api.authenticationMethod.dto.request.AuthenticationMethodUpdateRequest;
import com.findapi.api.authenticationMethod.dto.response.AuthenticationMethodDetailResponse;
import com.findapi.api.authenticationMethod.dto.response.AuthenticationMethodResponse;
import com.findapi.api.authenticationMethod.mapper.AuthenticationMethodMapper;
import com.findapi.api.authenticationMethod.repository.AuthenticationMethodRepository;
import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.common.exception.ResourceNotFoundException;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.entity.AuthenticationMethodEntity;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;

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
class AuthenticationMethodServiceTest {
    private static final UUID METHOD_ID = UUID.fromString("f4c39d81-4f3c-42e7-9342-d94143be9fd0");

    @Mock
    private AuthenticationMethodRepository authenticationMethodRepository;

    @Mock
    private AuthenticationMethodMapper authenticationMethodMapper;

    @Mock
    private EntityManager entityManager;

    @Mock
    private TypedQuery<Long> countQuery;

    private AuthenticationMethodService authenticationMethodService;

    @BeforeEach
    void setUp() {
        authenticationMethodService = new AuthenticationMethodService(
                authenticationMethodRepository,
                authenticationMethodMapper,
                entityManager
        );
    }

    @Test
    void createAuthenticationMethodSuccessfully() {
        AuthenticationMethodCreateRequest request = createRequest(" api_key ");
        AuthenticationMethodDetailResponse expected = detailResponse("API_KEY");

        when(authenticationMethodRepository.existsByNameAndDeletedAtIsNull("API_KEY")).thenReturn(false);
        when(authenticationMethodRepository.save(any(AuthenticationMethodEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(authenticationMethodMapper.entityToDetailResponse(any(AuthenticationMethodEntity.class)))
                .thenReturn(expected);

        AuthenticationMethodDetailResponse response = authenticationMethodService.create(request);

        ArgumentCaptor<AuthenticationMethodEntity> captor = ArgumentCaptor.forClass(AuthenticationMethodEntity.class);
        verify(authenticationMethodRepository).save(captor.capture());
        assertThat(response).isEqualTo(expected);
        assertThat(captor.getValue().getName()).isEqualTo("API_KEY");
    }

    @Test
    void blockDuplicatedNameOnCreate() {
        AuthenticationMethodCreateRequest request = createRequest("api_key");
        when(authenticationMethodRepository.existsByNameAndDeletedAtIsNull("API_KEY")).thenReturn(true);

        assertThatThrownBy(() -> authenticationMethodService.create(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Authentication method name already exists.");
    }

    @Test
    void findAuthenticationMethodByIdSuccessfully() {
        AuthenticationMethodEntity entity = activeEntity("API_KEY");
        AuthenticationMethodDetailResponse expected = detailResponse("API_KEY");

        when(authenticationMethodRepository.findByIdAndDeletedAtIsNull(METHOD_ID)).thenReturn(Optional.of(entity));
        when(authenticationMethodMapper.entityToDetailResponse(entity)).thenReturn(expected);

        assertThat(authenticationMethodService.findById(METHOD_ID)).isEqualTo(expected);
    }

    @Test
    void returnNotFoundWhenAuthenticationMethodIdDoesNotExist() {
        when(authenticationMethodRepository.findByIdAndDeletedAtIsNull(METHOD_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authenticationMethodService.findById(METHOD_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Authentication method not found.");
    }

    @Test
    void findAuthenticationMethodByNameSuccessfully() {
        AuthenticationMethodEntity entity = activeEntity("API_KEY");
        AuthenticationMethodDetailResponse expected = detailResponse("API_KEY");

        when(authenticationMethodRepository.findByNameAndDeletedAtIsNull("API_KEY")).thenReturn(Optional.of(entity));
        when(authenticationMethodMapper.entityToDetailResponse(entity)).thenReturn(expected);

        assertThat(authenticationMethodService.findByName("api_key")).isEqualTo(expected);
    }

    @Test
    void listAuthenticationMethodsWithFilters() {
        AuthenticationMethodEntity entity = activeEntity("API_KEY");
        AuthenticationMethodResponse mapped = response("API_KEY");
        AuthenticationMethodFilterRequest filter = AuthenticationMethodFilterRequest.builder().name("api").build();
        PageRequest request = PageRequest.of(0, 200, Sort.unsorted());

        when(authenticationMethodRepository.findAll(
                ArgumentMatchers.<Specification<AuthenticationMethodEntity>>any(),
                any(PageRequest.class)))
                .thenReturn(new PageImpl<>(
                        List.of(entity),
                        PageRequest.of(0, 100, Sort.by("createdAt").descending()),
                        1
                ));
        when(authenticationMethodMapper.entityToResponse(entity)).thenReturn(mapped);

        PageResponse<AuthenticationMethodResponse> page = authenticationMethodService.findAll(filter, request);

        assertThat(page.content()).containsExactly(mapped);
        assertThat(page.size()).isEqualTo(100);
        assertThat(page.totalElements()).isEqualTo(1);
    }

    @Test
    void updateAuthenticationMethodSuccessfully() {
        AuthenticationMethodEntity entity = activeEntity("API_KEY");
        AuthenticationMethodUpdateRequest request = updateRequest("bearer");
        AuthenticationMethodDetailResponse expected = detailResponse("BEARER");

        when(authenticationMethodRepository.findByIdAndDeletedAtIsNull(METHOD_ID)).thenReturn(Optional.of(entity));
        when(authenticationMethodRepository.existsByNameAndIdNotAndDeletedAtIsNull("BEARER", METHOD_ID))
                .thenReturn(false);
        when(authenticationMethodRepository.save(entity)).thenReturn(entity);
        when(authenticationMethodMapper.entityToDetailResponse(entity)).thenReturn(expected);

        AuthenticationMethodDetailResponse response = authenticationMethodService.update(METHOD_ID, request);

        assertThat(response).isEqualTo(expected);
        assertThat(entity.getName()).isEqualTo("BEARER");
    }

    @Test
    void blockDuplicatedNameOnUpdate() {
        AuthenticationMethodEntity entity = activeEntity("API_KEY");
        AuthenticationMethodUpdateRequest request = updateRequest("bearer");

        when(authenticationMethodRepository.findByIdAndDeletedAtIsNull(METHOD_ID)).thenReturn(Optional.of(entity));
        when(authenticationMethodRepository.existsByNameAndIdNotAndDeletedAtIsNull("BEARER", METHOD_ID))
                .thenReturn(true);

        assertThatThrownBy(() -> authenticationMethodService.update(METHOD_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Authentication method name already exists.");
    }

    @Test
    void removeAuthenticationMethodWithSoftDelete() {
        AuthenticationMethodEntity entity = activeEntity("API_KEY");

        when(authenticationMethodRepository.findByIdAndDeletedAtIsNull(METHOD_ID)).thenReturn(Optional.of(entity));
        when(entityManager.createQuery(any(String.class), eq(Long.class))).thenReturn(countQuery);
        when(countQuery.setParameter("id", METHOD_ID)).thenReturn(countQuery);
        when(countQuery.getSingleResult()).thenReturn(0L);

        authenticationMethodService.delete(METHOD_ID);

        assertThat(entity.getDeletedAt()).isNotNull();
        verify(authenticationMethodRepository).save(entity);
    }

    private AuthenticationMethodCreateRequest createRequest(String name) {
        return AuthenticationMethodCreateRequest.builder()
                .name(name)
                .build();
    }

    private AuthenticationMethodUpdateRequest updateRequest(String name) {
        return AuthenticationMethodUpdateRequest.builder()
                .name(name)
                .build();
    }

    private AuthenticationMethodEntity activeEntity(String name) {
        AuthenticationMethodEntity entity = new AuthenticationMethodEntity();
        entity.setId(METHOD_ID);
        entity.setName(name);
        return entity;
    }

    private AuthenticationMethodDetailResponse detailResponse(String name) {
        return AuthenticationMethodDetailResponse.builder()
                .id(METHOD_ID)
                .name(name)
                .build();
    }

    private AuthenticationMethodResponse response(String name) {
        return AuthenticationMethodResponse.builder()
                .id(METHOD_ID)
                .name(name)
                .build();
    }
}
