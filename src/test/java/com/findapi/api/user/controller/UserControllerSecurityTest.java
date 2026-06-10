package com.findapi.api.user.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.security.config.SecurityConfig;
import com.findapi.api.security.jwt.JwtAuthenticationConverterConfig;
import com.findapi.api.user.dto.request.UserFilterRequest;
import com.findapi.api.user.dto.response.UserResponse;
import com.findapi.api.user.service.UserService;
import com.findapi.api.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
@TestPropertySource(properties = "findapi.security.jwt.jwk-set-uri=https://issuer.example.test/jwks")
class UserControllerSecurityTest {
    private static final UUID USER_ID = UUID.randomUUID();

    @Autowired private MockMvc mockMvc;
    @MockitoBean private UserService service;

    @Test
    void blocksAnonymousProfile() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")).andExpect(status().isUnauthorized());
    }

    @Test
    void allowsAuthenticatedProfile() throws Exception {
        when(service.profile()).thenReturn(UserResponse.builder().id(USER_ID).build());
        mockMvc.perform(get("/api/v1/users/me")
                        .with(jwt().jwt(token -> token.subject(USER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isOk());
    }

    @Test
    void blocksUserFromAdminListing() throws Exception {
        mockMvc.perform(get("/api/v1/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAdminListing() throws Exception {
        when(service.findAll(any(UserFilterRequest.class), any(Pageable.class)))
                .thenReturn(new PageResponse<>(List.of(), 0, 20, 0, 0, true, true));
        mockMvc.perform(get("/api/v1/users")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isOk());
    }
}
