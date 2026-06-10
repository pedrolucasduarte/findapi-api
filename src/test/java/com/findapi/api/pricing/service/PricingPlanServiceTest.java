package com.findapi.api.pricing.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.findapi.api.apiCatalog.repository.ApiRepository;
import com.findapi.api.common.exception.BusinessException;
import com.findapi.api.common.exception.ResourceNotFoundException;
import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.entity.ApiEntity;
import com.findapi.api.entity.PricingPlanEntity;
import com.findapi.api.enums.BillingType;
import com.findapi.api.pricing.dto.request.PricingPlanCreateRequest;
import com.findapi.api.pricing.dto.request.PricingPlanFilterRequest;
import com.findapi.api.pricing.dto.request.PricingPlanUpdateRequest;
import com.findapi.api.pricing.dto.response.PricingPlanDetailResponse;
import com.findapi.api.pricing.dto.response.PricingPlanResponse;
import com.findapi.api.pricing.mapper.PricingPlanMapper;
import com.findapi.api.pricing.repository.PricingPlanRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class PricingPlanServiceTest {
    private static final UUID API_ID = UUID.fromString("4033709b-b0f8-4b74-8fd0-8c4d8f9f55f0");
    private static final UUID PLAN_ID = UUID.fromString("7f2ed709-0326-452a-9d01-833bfbd4ed65");

    @Mock
    private PricingPlanRepository pricingPlanRepository;

    @Mock
    private ApiRepository apiRepository;

    @Mock
    private PricingPlanMapper pricingPlanMapper;

    private PricingPlanService pricingPlanService;

    @BeforeEach
    void setUp() {
        pricingPlanService = new PricingPlanService(pricingPlanRepository, apiRepository, pricingPlanMapper);
    }

    @Test
    void createPricingPlanSuccessfully() {
        PricingPlanCreateRequest request = createRequest(BillingType.SUBSCRIPTION, new BigDecimal("29.90"));
        PricingPlanDetailResponse expected = detailResponse("Starter", BillingType.SUBSCRIPTION);

        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.of(apiEntity()));
        when(pricingPlanRepository.existsByApiIdAndNameIgnoreCaseAndDeletedAtIsNull(API_ID, "Starter"))
                .thenReturn(false);
        when(pricingPlanRepository.save(any(PricingPlanEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(pricingPlanMapper.entityToDetailResponse(any(PricingPlanEntity.class))).thenReturn(expected);

        PricingPlanDetailResponse response = pricingPlanService.create(API_ID, request);

        ArgumentCaptor<PricingPlanEntity> captor = ArgumentCaptor.forClass(PricingPlanEntity.class);
        verify(pricingPlanRepository).save(captor.capture());
        assertThat(response).isEqualTo(expected);
        assertThat(captor.getValue().getApi().getId()).isEqualTo(API_ID);
        assertThat(captor.getValue().getName()).isEqualTo("Starter");
        assertThat(captor.getValue().getCurrency()).isEqualTo("USD");
    }

    @Test
    void blockCreateWhenApiDoesNotExist() {
        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pricingPlanService.create(
                API_ID,
                createRequest(BillingType.SUBSCRIPTION, new BigDecimal("29.90"))
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("API not found.");
    }

    @Test
    void blockCreateWhenApiIsDeleted() {
        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pricingPlanService.create(
                API_ID,
                createRequest(BillingType.SUBSCRIPTION, new BigDecimal("29.90"))
        ))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("API not found.");
    }

    @Test
    void blockNegativePrice() {
        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.of(apiEntity()));
        when(pricingPlanRepository.existsByApiIdAndNameIgnoreCaseAndDeletedAtIsNull(API_ID, "Starter"))
                .thenReturn(false);

        assertThatThrownBy(() -> pricingPlanService.create(
                API_ID,
                createRequest(BillingType.SUBSCRIPTION, new BigDecimal("-1.00"))
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Price cannot be negative.");
    }

    @Test
    void blockFreePlanWithPositivePrice() {
        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.of(apiEntity()));
        when(pricingPlanRepository.existsByApiIdAndNameIgnoreCaseAndDeletedAtIsNull(API_ID, "Starter"))
                .thenReturn(false);

        assertThatThrownBy(() -> pricingPlanService.create(
                API_ID,
                createRequest(BillingType.FREE, new BigDecimal("1.00"))
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessage("FREE pricing plans cannot have a positive price.");
    }

    @Test
    void findPricingPlanByIdSuccessfully() {
        PricingPlanEntity entity = activeEntity("Starter", BillingType.SUBSCRIPTION);
        PricingPlanDetailResponse expected = detailResponse("Starter", BillingType.SUBSCRIPTION);

        when(pricingPlanRepository.findByIdAndDeletedAtIsNull(PLAN_ID)).thenReturn(Optional.of(entity));
        when(pricingPlanMapper.entityToDetailResponse(entity)).thenReturn(expected);

        assertThat(pricingPlanService.findById(PLAN_ID)).isEqualTo(expected);
    }

    @Test
    void returnNotFoundWhenPricingPlanIdDoesNotExist() {
        when(pricingPlanRepository.findByIdAndDeletedAtIsNull(PLAN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> pricingPlanService.findById(PLAN_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Pricing plan not found.");
    }

    @Test
    void listPricingPlansByApiId() {
        PricingPlanEntity entity = activeEntity("Starter", BillingType.SUBSCRIPTION);
        PricingPlanResponse mapped = response("Starter", BillingType.SUBSCRIPTION);

        when(apiRepository.findByIdAndDeletedAtIsNull(API_ID)).thenReturn(Optional.of(apiEntity()));
        when(pricingPlanRepository.findByApiIdAndDeletedAtIsNull(API_ID)).thenReturn(List.of(entity));
        when(pricingPlanMapper.entityToResponse(entity)).thenReturn(mapped);

        assertThat(pricingPlanService.findByApiId(API_ID)).containsExactly(mapped);
    }

    @Test
    void listPricingPlansWithFilters() {
        PricingPlanEntity entity = activeEntity("Starter", BillingType.SUBSCRIPTION);
        PricingPlanResponse mapped = response("Starter", BillingType.SUBSCRIPTION);
        PricingPlanFilterRequest filter = PricingPlanFilterRequest.builder()
                .apiId(API_ID)
                .billingType(BillingType.SUBSCRIPTION)
                .currency("usd")
                .minPrice(new BigDecimal("10.00"))
                .maxPrice(new BigDecimal("30.00"))
                .build();
        PageRequest request = PageRequest.of(0, 200, Sort.unsorted());

        when(pricingPlanRepository.findAll(
                ArgumentMatchers.<Specification<PricingPlanEntity>>any(),
                any(PageRequest.class)))
                .thenReturn(new PageImpl<>(
                        List.of(entity),
                        PageRequest.of(0, 100, Sort.by("createdAt").descending()),
                        1
                ));
        when(pricingPlanMapper.entityToResponse(entity)).thenReturn(mapped);

        PageResponse<PricingPlanResponse> page = pricingPlanService.findAll(filter, request);

        assertThat(page.content()).containsExactly(mapped);
        assertThat(page.size()).isEqualTo(100);
    }

    @Test
    void updatePricingPlanSuccessfully() {
        PricingPlanEntity entity = activeEntity("Starter", BillingType.SUBSCRIPTION);
        PricingPlanUpdateRequest request = updateRequest("Growth");
        PricingPlanDetailResponse expected = detailResponse("Growth", BillingType.USAGE_BASED);

        when(pricingPlanRepository.findByIdAndDeletedAtIsNull(PLAN_ID)).thenReturn(Optional.of(entity));
        when(pricingPlanRepository.existsByApiIdAndNameIgnoreCaseAndIdNotAndDeletedAtIsNull(
                API_ID,
                "Growth",
                PLAN_ID
        )).thenReturn(false);
        when(pricingPlanRepository.save(entity)).thenReturn(entity);
        when(pricingPlanMapper.entityToDetailResponse(entity)).thenReturn(expected);

        PricingPlanDetailResponse response = pricingPlanService.update(PLAN_ID, request);

        assertThat(response).isEqualTo(expected);
        assertThat(entity.getName()).isEqualTo("Growth");
        assertThat(entity.getBillingType()).isEqualTo(BillingType.USAGE_BASED);
        assertThat(entity.getCurrency()).isEqualTo("BRL");
    }

    @Test
    void blockDuplicatedNameForSameApiOnUpdate() {
        PricingPlanEntity entity = activeEntity("Starter", BillingType.SUBSCRIPTION);
        PricingPlanUpdateRequest request = updateRequest("Growth");

        when(pricingPlanRepository.findByIdAndDeletedAtIsNull(PLAN_ID)).thenReturn(Optional.of(entity));
        when(pricingPlanRepository.existsByApiIdAndNameIgnoreCaseAndIdNotAndDeletedAtIsNull(
                API_ID,
                "Growth",
                PLAN_ID
        )).thenReturn(true);

        assertThatThrownBy(() -> pricingPlanService.update(PLAN_ID, request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Pricing plan name already exists for this API.");
    }

    @Test
    void removePricingPlanWithSoftDelete() {
        PricingPlanEntity entity = activeEntity("Starter", BillingType.SUBSCRIPTION);
        when(pricingPlanRepository.findByIdAndDeletedAtIsNull(PLAN_ID)).thenReturn(Optional.of(entity));

        pricingPlanService.delete(PLAN_ID);

        assertThat(entity.getDeletedAt()).isNotNull();
        verify(pricingPlanRepository).save(entity);
    }

    private PricingPlanCreateRequest createRequest(BillingType billingType, BigDecimal price) {
        return PricingPlanCreateRequest.builder()
                .name("Starter")
                .billingType(billingType)
                .freeLimit("1k calls")
                .price(price)
                .currency("usd")
                .description("Starter plan")
                .build();
    }

    private PricingPlanUpdateRequest updateRequest(String name) {
        return PricingPlanUpdateRequest.builder()
                .name(name)
                .billingType(BillingType.USAGE_BASED)
                .freeLimit("10k calls")
                .price(new BigDecimal("19.90"))
                .currency("brl")
                .description("Growth plan")
                .build();
    }

    private PricingPlanEntity activeEntity(String name, BillingType billingType) {
        PricingPlanEntity entity = new PricingPlanEntity();
        entity.setId(PLAN_ID);
        entity.setApi(apiEntity());
        entity.setName(name);
        entity.setBillingType(billingType);
        entity.setPrice(new BigDecimal("29.90"));
        entity.setCurrency("USD");
        return entity;
    }

    private ApiEntity apiEntity() {
        ApiEntity api = new ApiEntity();
        api.setId(API_ID);
        api.setName("Find API");
        api.setSlug("find-api");
        return api;
    }

    private PricingPlanDetailResponse detailResponse(String name, BillingType billingType) {
        return PricingPlanDetailResponse.builder()
                .id(PLAN_ID)
                .apiId(API_ID)
                .name(name)
                .billingType(billingType)
                .price(new BigDecimal("29.90"))
                .currency("USD")
                .build();
    }

    private PricingPlanResponse response(String name, BillingType billingType) {
        return PricingPlanResponse.builder()
                .id(PLAN_ID)
                .apiId(API_ID)
                .name(name)
                .billingType(billingType)
                .price(new BigDecimal("29.90"))
                .currency("USD")
                .build();
    }
}
