package com.findapi.api.category.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findapi.api.category.dto.request.CategoryCreateRequest;
import com.findapi.api.category.dto.request.CategoryFilterRequest;
import com.findapi.api.category.dto.response.CategoryDetailResponse;
import com.findapi.api.category.dto.response.CategoryResponse;
import com.findapi.api.category.service.CategoryService;
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
class CategoryControllerSecurityTest {
    private static final UUID CATEGORY_ID = UUID.fromString("235f8d5e-23f5-4aae-bae6-f66ce573db1f");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private CategoryService categoryService;

    @Test
    void allowsPublicCategoryListingWithoutJwt() throws Exception {
        when(categoryService.findAll(any(CategoryFilterRequest.class), any(Pageable.class)))
                .thenReturn(new PageResponse<>(
                        List.of(CategoryResponse.builder().id(CATEGORY_ID).slug("developer-tools").build()),
                        0,
                        20,
                        1,
                        1,
                        true,
                        true
                ));

        mockMvc.perform(get("/api/v1/categories"))
                .andExpect(status().isOk());
    }

    @Test
    void allowsAdminToCreateCategory() throws Exception {
        when(categoryService.create(any(CategoryCreateRequest.class))).thenReturn(detailResponse());

        mockMvc.perform(post("/api/v1/categories")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    void blocksUserFromCreatingCategory() throws Exception {
        mockMvc.perform(post("/api/v1/categories")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isForbidden());
    }

    private CategoryCreateRequest createRequest() {
        return CategoryCreateRequest.builder()
                .name("Developer Tools")
                .slug("developer-tools")
                .build();
    }

    private CategoryDetailResponse detailResponse() {
        return CategoryDetailResponse.builder()
                .id(CATEGORY_ID)
                .name("Developer Tools")
                .slug("developer-tools")
                .build();
    }
}
