package com.findapi.api.collection.controller;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.findapi.api.collection.service.CollectionApiService;
import com.findapi.api.security.config.SecurityConfig;
import com.findapi.api.security.jwt.JwtAuthenticationConverterConfig;
import com.findapi.api.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "findapi.security.jwt.jwk-set-uri=https://issuer.example.test/jwks")
class CollectionApiControllerSecurityTest {
    private static final UUID COLLECTION_ID = UUID.randomUUID();
    private static final UUID API_ID = UUID.randomUUID();

    @Autowired private MockMvc mockMvc;
    @MockitoBean private CollectionApiService service;

    @Test
    void allowsPublicApiListing() throws Exception {
        when(service.findApis(COLLECTION_ID)).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/collections/{collectionId}/apis", COLLECTION_ID))
                .andExpect(status().isOk());
    }

    @Test
    void blocksAnonymousAdd() throws Exception {
        mockMvc.perform(post("/api/v1/collections/{collectionId}/apis/{apiId}", COLLECTION_ID, API_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void allowsAuthenticatedAdd() throws Exception {
        doNothing().when(service).addApi(COLLECTION_ID, API_ID);
        mockMvc.perform(post("/api/v1/collections/{collectionId}/apis/{apiId}", COLLECTION_ID, API_ID)
                        .with(jwt().jwt(token -> token.subject(UUID.randomUUID().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNoContent());
    }
}
