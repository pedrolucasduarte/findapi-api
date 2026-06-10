package com.findapi.api.pricing.service;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Locale;
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
import com.findapi.api.pricing.specification.PricingPlanSpecification;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PricingPlanService {
    private static final int MAX_PAGE_SIZE = 100;
    private static final int DEFAULT_PAGE_SIZE = 20;
    private static final String PRICING_PLAN_NOT_FOUND = "Pricing plan not found.";
    private static final String API_NOT_FOUND = "API not found.";
    private static final String PLAN_NAME_ALREADY_EXISTS = "Pricing plan name already exists for this API.";
    private static final String INVALID_FREE_PRICE = "FREE pricing plans cannot have a positive price.";
    private static final String CURRENCY_REQUIRED = "Currency is required when price is informed.";

    private final PricingPlanRepository pricingPlanRepository;
    private final ApiRepository apiRepository;
    private final PricingPlanMapper pricingPlanMapper;

    @Transactional
    public PricingPlanDetailResponse create(UUID apiId, PricingPlanCreateRequest request) {
        ApiEntity api = findActiveApi(apiId);
        String normalizedName = clean(request.getName());
        assertNameAvailable(apiId, normalizedName);

        PricingPlanEntity entity = new PricingPlanEntity();
        entity.setApi(api);
        applyCreateRequest(entity, request, normalizedName);
        return pricingPlanMapper.entityToDetailResponse(pricingPlanRepository.save(entity));
    }

    @Transactional(readOnly = true)
    public PricingPlanDetailResponse findById(UUID id) {
        return pricingPlanMapper.entityToDetailResponse(findActiveEntity(id));
    }

    @Transactional(readOnly = true)
    public List<PricingPlanResponse> findByApiId(UUID apiId) {
        findActiveApi(apiId);
        return pricingPlanRepository.findByApiIdAndDeletedAtIsNull(apiId)
                .stream()
                .map(pricingPlanMapper::entityToResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResponse<PricingPlanResponse> findAll(PricingPlanFilterRequest filter, Pageable pageable) {
        Pageable safePageable = safePageable(pageable);
        return PageResponse.from(pricingPlanRepository.findAll(PricingPlanSpecification.fromFilter(filter), safePageable)
                .map(pricingPlanMapper::entityToResponse));
    }

    @Transactional
    public PricingPlanDetailResponse update(UUID id, PricingPlanUpdateRequest request) {
        PricingPlanEntity entity = findActiveEntity(id);
        UUID apiId = entity.getApi().getId();
        String normalizedName = clean(request.getName());

        if (pricingPlanRepository.existsByApiIdAndNameIgnoreCaseAndIdNotAndDeletedAtIsNull(apiId, normalizedName, id)) {
            throw new BusinessException(PLAN_NAME_ALREADY_EXISTS);
        }

        applyUpdateRequest(entity, request, normalizedName);
        return pricingPlanMapper.entityToDetailResponse(pricingPlanRepository.save(entity));
    }

    @Transactional
    public void delete(UUID id) {
        PricingPlanEntity entity = findActiveEntity(id);
        entity.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        pricingPlanRepository.save(entity);
    }

    private void applyCreateRequest(PricingPlanEntity entity, PricingPlanCreateRequest request, String normalizedName) {
        validatePricing(request.getBillingType(), request.getPrice(), request.getCurrency());
        entity.setName(normalizedName);
        entity.setBillingType(request.getBillingType());
        entity.setFreeLimit(clean(request.getFreeLimit()));
        entity.setPrice(request.getPrice());
        entity.setCurrency(normalizeCurrency(request.getCurrency()));
        entity.setDescription(clean(request.getDescription()));
    }

    private void applyUpdateRequest(PricingPlanEntity entity, PricingPlanUpdateRequest request, String normalizedName) {
        validatePricing(request.getBillingType(), request.getPrice(), request.getCurrency());
        entity.setName(normalizedName);
        entity.setBillingType(request.getBillingType());
        entity.setFreeLimit(clean(request.getFreeLimit()));
        entity.setPrice(request.getPrice());
        entity.setCurrency(normalizeCurrency(request.getCurrency()));
        entity.setDescription(clean(request.getDescription()));
    }

    private ApiEntity findActiveApi(UUID id) {
        return apiRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(API_NOT_FOUND));
    }

    private PricingPlanEntity findActiveEntity(UUID id) {
        return pricingPlanRepository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new ResourceNotFoundException(PRICING_PLAN_NOT_FOUND));
    }

    private void assertNameAvailable(UUID apiId, String name) {
        if (pricingPlanRepository.existsByApiIdAndNameIgnoreCaseAndDeletedAtIsNull(apiId, name)) {
            throw new BusinessException(PLAN_NAME_ALREADY_EXISTS);
        }
    }

    private void validatePricing(BillingType billingType, BigDecimal price, String currency) {
        if (price != null && price.signum() < 0) {
            throw new BusinessException("Price cannot be negative.");
        }
        if (BillingType.FREE.equals(billingType) && price != null && price.signum() > 0) {
            throw new BusinessException(INVALID_FREE_PRICE);
        }
        if (price != null && !hasText(currency)) {
            throw new BusinessException(CURRENCY_REQUIRED);
        }
    }

    private Pageable safePageable(Pageable pageable) {
        if (pageable.isUnpaged()) {
            return PageRequest.of(0, DEFAULT_PAGE_SIZE, Sort.by(Sort.Direction.DESC, "createdAt"));
        }

        int pageNumber = Math.max(pageable.getPageNumber(), 0);
        int pageSize = Math.min(pageable.getPageSize(), MAX_PAGE_SIZE);
        Sort sort = pageable.getSort().isSorted()
                ? pageable.getSort()
                : Sort.by(Sort.Direction.DESC, "createdAt");
        return PageRequest.of(pageNumber, pageSize, sort);
    }

    private String normalizeCurrency(String currency) {
        return hasText(currency) ? currency.trim().toUpperCase(Locale.ROOT) : null;
    }

    private String clean(String value) {
        return value == null ? null : value.trim();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
