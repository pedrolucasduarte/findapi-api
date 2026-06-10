package com.findapi.api.authenticationMethod.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findapi.api.authenticationMethod.dto.request.AuthenticationMethodCreateRequest;
import com.findapi.api.authenticationMethod.dto.request.AuthenticationMethodFilterRequest;
import com.findapi.api.authenticationMethod.dto.response.AuthenticationMethodDetailResponse;
import com.findapi.api.authenticationMethod.dto.response.AuthenticationMethodResponse;
import com.findapi.api.authenticationMethod.service.AuthenticationMethodService;
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
class AuthenticationMethodControllerSecurityTest {
    private static final UUID METHOD_ID = UUID.fromString("f4c39d81-4f3c-42e7-9342-d94143be9fd0");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private AuthenticationMethodService authenticationMethodService;

    @Test
    void allowsPublicAuthenticationMethodListingWithoutJwt() throws Exception {
        when(authenticationMethodService.findAll(
                any(AuthenticationMethodFilterRequest.class),
                any(Pageable.class)
        )).thenReturn(new PageResponse<>(
                List.of(AuthenticationMethodResponse.builder().id(METHOD_ID).name("API_KEY").build()),
                0,
                20,
                1,
                1,
                true,
                true
        ));

        mockMvc.perform(get("/api/v1/authentication-methods"))
                .andExpect(status().isOk());
    }

    @Test
    void allowsAdminToCreateAuthenticationMethod() throws Exception {
        when(authenticationMethodService.create(any(AuthenticationMethodCreateRequest.class)))
                .thenReturn(detailResponse());

        mockMvc.perform(post("/api/v1/authentication-methods")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    void blocksUserFromCreatingAuthenticationMethod() throws Exception {
        mockMvc.perform(post("/api/v1/authentication-methods")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isForbidden());
    }

    private AuthenticationMethodCreateRequest createRequest() {
        return AuthenticationMethodCreateRequest.builder()
                .name("API_KEY")
                .build();
    }

    private AuthenticationMethodDetailResponse detailResponse() {
        return AuthenticationMethodDetailResponse.builder()
                .id(METHOD_ID)
                .name("API_KEY")
                .build();
    }
}
