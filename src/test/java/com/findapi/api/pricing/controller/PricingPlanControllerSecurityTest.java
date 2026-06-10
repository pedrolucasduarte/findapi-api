package com.findapi.api.pricing.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.enums.BillingType;
import com.findapi.api.pricing.dto.request.PricingPlanCreateRequest;
import com.findapi.api.pricing.dto.request.PricingPlanFilterRequest;
import com.findapi.api.pricing.dto.response.PricingPlanDetailResponse;
import com.findapi.api.pricing.dto.response.PricingPlanResponse;
import com.findapi.api.pricing.service.PricingPlanService;
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
class PricingPlanControllerSecurityTest {
    private static final UUID API_ID = UUID.fromString("4033709b-b0f8-4b74-8fd0-8c4d8f9f55f0");
    private static final UUID PLAN_ID = UUID.fromString("7f2ed709-0326-452a-9d01-833bfbd4ed65");

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @MockitoBean
    private PricingPlanService pricingPlanService;

    @Test
    void allowsPublicPricingPlanListingWithoutJwt() throws Exception {
        when(pricingPlanService.findAll(any(PricingPlanFilterRequest.class), any(Pageable.class)))
                .thenReturn(new PageResponse<>(
                        List.of(response()),
                        0,
                        20,
                        1,
                        1,
                        true,
                        true
                ));

        mockMvc.perform(get("/api/v1/pricing-plans"))
                .andExpect(status().isOk());
    }

    @Test
    void allowsPublicApiPricingPlanListingWithoutJwt() throws Exception {
        when(pricingPlanService.findByApiId(API_ID)).thenReturn(List.of(response()));

        mockMvc.perform(get("/api/v1/apis/{apiId}/pricing-plans", API_ID))
                .andExpect(status().isOk());
    }

    @Test
    void allowsProviderToCreatePricingPlan() throws Exception {
        when(pricingPlanService.create(eq(API_ID), any(PricingPlanCreateRequest.class))).thenReturn(detailResponse());

        mockMvc.perform(post("/api/v1/apis/{apiId}/pricing-plans", API_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROVIDER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isCreated());
    }

    @Test
    void blocksUserFromCreatingPricingPlan() throws Exception {
        mockMvc.perform(post("/api/v1/apis/{apiId}/pricing-plans", API_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isForbidden());
    }

    @Test
    void blocksProviderFromDeletingPricingPlan() throws Exception {
        mockMvc.perform(delete("/api/v1/pricing-plans/{id}", PLAN_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PROVIDER"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void allowsAdminToDeletePricingPlan() throws Exception {
        doNothing().when(pricingPlanService).delete(PLAN_ID);

        mockMvc.perform(delete("/api/v1/pricing-plans/{id}", PLAN_ID)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
                .andExpect(status().isNoContent());
    }

    private PricingPlanCreateRequest createRequest() {
        return PricingPlanCreateRequest.builder()
                .name("Starter")
                .billingType(BillingType.SUBSCRIPTION)
                .price(new BigDecimal("29.90"))
                .currency("USD")
                .build();
    }

    private PricingPlanResponse response() {
        return PricingPlanResponse.builder()
                .id(PLAN_ID)
                .apiId(API_ID)
                .name("Starter")
                .billingType(BillingType.SUBSCRIPTION)
                .price(new BigDecimal("29.90"))
                .currency("USD")
                .build();
    }

    private PricingPlanDetailResponse detailResponse() {
        return PricingPlanDetailResponse.builder()
                .id(PLAN_ID)
                .apiId(API_ID)
                .name("Starter")
                .billingType(BillingType.SUBSCRIPTION)
                .price(new BigDecimal("29.90"))
                .currency("USD")
                .build();
    }
}
