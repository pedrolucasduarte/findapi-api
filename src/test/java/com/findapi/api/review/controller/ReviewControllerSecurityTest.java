package com.findapi.api.review.controller;

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
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.review.dto.request.ReviewCreateRequest;
import com.findapi.api.review.dto.request.ReviewFilterRequest;
import com.findapi.api.review.dto.response.ReviewDetailResponse;
import com.findapi.api.review.dto.response.ReviewResponse;
import com.findapi.api.review.service.ReviewService;
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
class ReviewControllerSecurityTest {
    private static final UUID API_ID = UUID.fromString("4033709b-b0f8-4b74-8fd0-8c4d8f9f55f0");
    private static final UUID REVIEW_ID = UUID.fromString("7f2ed709-0326-452a-9d01-833bfbd4ed65");
    private static final UUID USER_ID = UUID.fromString("a9ef0f06-d716-4419-b7a9-58c820cc4894");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private ReviewService reviewService;

    @Test
    void allowsPublicReviewListingWithoutJwt() throws Exception {
        when(reviewService.findAll(any(ReviewFilterRequest.class), any(Pageable.class)))
                .thenReturn(new PageResponse<>(
                        List.of(response()),
                        0,
                        20,
                        1,
                        1,
                        true,
                        true
                ));

        mockMvc.perform(get("/api/v1/reviews"))
                .andExpect(status().isOk());
    }

    @Test
    void allowsPublicApiReviewListingWithoutJwt() throws Exception {
        when(reviewService.findByApiId(API_ID)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/apis/{apiId}/reviews", API_ID))
                .andExpect(status().isOk());
    }

    @Test
    void allowsUserToCreateReview() throws Exception {
        when(reviewService.create(eq(API_ID), any(ReviewCreateRequest.class))).thenReturn(detailResponse());

        mockMvc.perform(post("/api/v1/apis/{apiId}/reviews", API_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    void blocksAnonymousFromCreatingReview() throws Exception {
        mockMvc.perform(post("/api/v1/apis/{apiId}/reviews", API_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void allowsAuthenticatedUserToRequestDeleteReview() throws Exception {
        doNothing().when(reviewService).delete(REVIEW_ID);

        mockMvc.perform(delete("/api/v1/reviews/{id}", REVIEW_ID)
                        .with(jwt().jwt(jwt -> jwt.subject(USER_ID.toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_USER"))))
                .andExpect(status().isNoContent());
    }

    private ReviewCreateRequest createRequest() {
        return ReviewCreateRequest.builder()
                .rating(5)
                .comment("Great API")
                .build();
    }

    private ReviewResponse response() {
        return ReviewResponse.builder()
                .id(REVIEW_ID)
                .apiId(API_ID)
                .userId(USER_ID)
                .rating(5)
                .comment("Great API")
                .build();
    }

    private ReviewDetailResponse detailResponse() {
        return ReviewDetailResponse.builder()
                .id(REVIEW_ID)
                .apiId(API_ID)
                .userId(USER_ID)
                .rating(5)
                .comment("Great API")
                .build();
    }
}
