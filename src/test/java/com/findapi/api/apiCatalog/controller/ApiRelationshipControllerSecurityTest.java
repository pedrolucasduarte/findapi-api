package com.findapi.api.apiCatalog.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.findapi.api.apiCatalog.service.ApiRelationshipService;
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
class ApiRelationshipControllerSecurityTest {
    private static final UUID API_ID = UUID.randomUUID();
    private static final UUID CATEGORY_ID = UUID.randomUUID();

    @Autowired private MockMvc mockMvc;
    @MockitoBean private ApiRelationshipService service;

    @Test
    void allowsPublicRelationshipListing() throws Exception {
        when(service.findCategories(API_ID)).thenReturn(List.of());
        mockMvc.perform(get("/api/v1/apis/{apiId}/categories", API_ID))
                .andExpect(status().isOk());
    }

    @Test
    void blocksAnonymousRelationshipCreation() throws Exception {
        mockMvc.perform(post("/api/v1/apis/{apiId}/categories/{categoryId}", API_ID, CATEGORY_ID))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void blocksUserRelationshipCreation() throws Exception {
        mockMvc.perform(post("/api/v1/apis/{apiId}/categories/{categoryId}", API_ID, CATEGORY_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsProviderRelationshipCreation() throws Exception {
        doNothing().when(service).addCategory(any(UUID.class), any(UUID.class));
        mockMvc.perform(post("/api/v1/apis/{apiId}/categories/{categoryId}", API_ID, CATEGORY_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROVIDER"))))
                .andExpect(status().isNoContent());
    }
}
