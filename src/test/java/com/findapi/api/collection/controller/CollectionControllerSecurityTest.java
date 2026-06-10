package com.findapi.api.collection.controller;

import static org.mockito.ArgumentMatchers.any;
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
import com.findapi.api.collection.dto.request.CollectionCreateRequest;
import com.findapi.api.collection.dto.request.CollectionFilterRequest;
import com.findapi.api.collection.dto.response.CollectionDetailResponse;
import com.findapi.api.collection.dto.response.CollectionResponse;
import com.findapi.api.collection.service.CollectionService;
import com.findapi.api.common.pagination.PageResponse;
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
class CollectionControllerSecurityTest {
    private static final UUID COLLECTION_ID = UUID.fromString("1e132356-95b6-4e18-85e7-6f9a326342b1");
    private static final UUID OWNER_ID = UUID.fromString("a9ef0f06-d716-4419-b7a9-58c820cc4894");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CollectionService collectionService;

    @Test
    void allowsPublicCollectionListingWithoutJwt() throws Exception {
        when(collectionService.findAll(any(CollectionFilterRequest.class), any(Pageable.class)))
                .thenReturn(new PageResponse<>(
                        List.of(response()),
                        0,
                        20,
                        1,
                        1,
                        true,
                        true
                ));

        mockMvc.perform(get("/api/v1/collections"))
                .andExpect(status().isOk());
    }

    @Test
    void allowsPublicCollectionDetailsWithoutJwt() throws Exception {
        when(collectionService.findById(COLLECTION_ID)).thenReturn(detailResponse());

        mockMvc.perform(get("/api/v1/collections/{id}", COLLECTION_ID))
                .andExpect(status().isOk());
    }

    @Test
    void allowsUserToCreateCollection() throws Exception {
        when(collectionService.create(any(CollectionCreateRequest.class))).thenReturn(detailResponse());

        mockMvc.perform(post("/api/v1/collections")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    void blocksAnonymousFromCreatingCollection() throws Exception {
        mockMvc.perform(post("/api/v1/collections")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void allowsAuthenticatedUserToRequestDeleteCollection() throws Exception {
        doNothing().when(collectionService).delete(COLLECTION_ID);

        mockMvc.perform(delete("/api/v1/collections/{id}", COLLECTION_ID)
                        .with(jwt().jwt(jwt -> jwt.subject(OWNER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNoContent());
    }

    private CollectionCreateRequest createRequest() {
        return CollectionCreateRequest.builder()
                .name("Dev Tools")
                .slug("dev-tools")
                .description("Useful APIs")
                .build();
    }

    private CollectionResponse response() {
        return CollectionResponse.builder()
                .id(COLLECTION_ID)
                .ownerId(OWNER_ID)
                .name("Dev Tools")
                .slug("dev-tools")
                .description("Useful APIs")
                .build();
    }

    private CollectionDetailResponse detailResponse() {
        return CollectionDetailResponse.builder()
                .id(COLLECTION_ID)
                .ownerId(OWNER_ID)
                .name("Dev Tools")
                .slug("dev-tools")
                .description("Useful APIs")
                .build();
    }
}
