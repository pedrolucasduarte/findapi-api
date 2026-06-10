package com.findapi.api.apiCatalog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findapi.api.apiCatalog.dto.request.ApiCreateRequest;
import com.findapi.api.apiCatalog.dto.request.ApiFilterRequest;
import com.findapi.api.apiCatalog.dto.response.ApiDetailResponse;
import com.findapi.api.apiCatalog.dto.response.ApiResponse;
import com.findapi.api.apiCatalog.service.ApiCatalogService;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.enums.ApiStatus;
import com.findapi.api.enums.ApiType;
import com.findapi.api.enums.IntegrationDifficulty;
import com.findapi.api.security.config.SecurityConfig;
import com.findapi.api.security.jwt.JwtAuthenticationConverterConfig;
import com.findapi.api.TestcontainersConfiguration;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "findapi.security.jwt.jwk-set-uri=https://issuer.example.test/jwks")
class ApiCatalogControllerSecurityTest {
    private static final UUID API_ID = UUID.fromString("3b383a91-c940-46a4-89fe-8f912558ac8f");
    private static final UUID AUTHENTICATION_METHOD_ID = UUID.fromString("7cb2ff94-69d5-4da1-88b1-84d54dc3948d");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();


    @MockitoBean
    private ApiCatalogService apiCatalogService;

    @Test
    void allowsPublicApiListingWithoutJwt() throws Exception {
        when(apiCatalogService.list(any(ApiFilterRequest.class), any(Pageable.class)))
                .thenReturn(new PageResponse<>(
                        List.of(ApiResponse.builder().id(API_ID).slug("open-weather").build()),
                        0,
                        20,
                        1,
                        1,
                        true,
                        true
                ));

        mockMvc.perform(get("/api/v1/apis"))
                .andExpect(status().isOk());
    }

    @Test
    void allowsProviderToCreateApi() throws Exception {
        when(apiCatalogService.create(any(ApiCreateRequest.class))).thenReturn(detailResponse());

        mockMvc.perform(post("/api/v1/apis")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROVIDER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    void blocksUserFromCreatingApi() throws Exception {
        mockMvc.perform(post("/api/v1/apis")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void blocksProviderFromDeletingApi() throws Exception {
        mockMvc.perform(delete("/api/v1/apis/{id}", API_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROVIDER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAdminToDeleteApi() throws Exception {
        doNothing().when(apiCatalogService).delete(eq(API_ID));

        mockMvc.perform(delete("/api/v1/apis/{id}", API_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    private ApiCreateRequest createRequest() {
        return ApiCreateRequest.builder()
                .name("Open Weather")
                .slug("open-weather")
                .shortDescription("Weather API")
                .fullDescription("Weather API details")
                .officialSite("https://example.com")
                .documentationUrl("https://docs.example.com")
                .apiType(ApiType.PUBLIC)
                .status(ApiStatus.ACTIVE)
                .freeTier(true)
                .officialSdk(true)
                .openSource(false)
                .selfHosted(false)
                .brazilian(false)
                .integrationDifficulty(IntegrationDifficulty.EASY)
                .authenticationMethodId(AUTHENTICATION_METHOD_ID)
                .build();
    }

    private ApiDetailResponse detailResponse() {
        return ApiDetailResponse.builder()
                .id(API_ID)
                .name("Open Weather")
                .slug("open-weather")
                .shortDescription("Weather API")
                .apiType(ApiType.PUBLIC)
                .status(ApiStatus.ACTIVE)
                .freeTier(true)
                .officialSdk(true)
                .integrationDifficulty(IntegrationDifficulty.EASY)
                .authenticationMethodId(AUTHENTICATION_METHOD_ID)
                .authenticationMethodName("API_KEY")
                .build();
    }
}
