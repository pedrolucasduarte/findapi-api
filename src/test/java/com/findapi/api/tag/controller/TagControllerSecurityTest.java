package com.findapi.api.tag.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.security.config.SecurityConfig;
import com.findapi.api.security.jwt.JwtAuthenticationConverterConfig;
import com.findapi.api.TestcontainersConfiguration;
import com.findapi.api.tag.dto.request.TagCreateRequest;
import com.findapi.api.tag.dto.request.TagFilterRequest;
import com.findapi.api.tag.dto.response.TagDetailResponse;
import com.findapi.api.tag.dto.response.TagResponse;
import com.findapi.api.tag.service.TagService;

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
class TagControllerSecurityTest {
    private static final UUID TAG_ID = UUID.fromString("d4fb92b8-b53c-4f25-b089-9e7d433c7f4d");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private TagService tagService;

    @Test
    void allowsPublicTagListingWithoutJwt() throws Exception {
        when(tagService.findAll(any(TagFilterRequest.class), any(Pageable.class)))
                .thenReturn(new PageResponse<>(
                        List.of(TagResponse.builder().id(TAG_ID).slug("spring-boot").build()),
                        0,
                        20,
                        1,
                        1,
                        true,
                        true
                ));

        mockMvc.perform(get("/api/v1/tags"))
                .andExpect(status().isOk());
    }

    @Test
    void allowsAdminToCreateTag() throws Exception {
        when(tagService.create(any(TagCreateRequest.class))).thenReturn(detailResponse());

        mockMvc.perform(post("/api/v1/tags")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    void blocksUserFromCreatingTag() throws Exception {
        mockMvc.perform(post("/api/v1/tags")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isForbidden());
    }

    private TagCreateRequest createRequest() {
        return TagCreateRequest.builder()
                .name("Spring Boot")
                .slug("spring-boot")
                .build();
    }

    private TagDetailResponse detailResponse() {
        return TagDetailResponse.builder()
                .id(TAG_ID)
                .name("Spring Boot")
                .slug("spring-boot")
                .build();
    }
}
