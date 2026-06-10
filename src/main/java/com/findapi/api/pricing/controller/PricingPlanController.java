package com.findapi.api.pricing.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.pricing.dto.request.PricingPlanCreateRequest;
import com.findapi.api.pricing.dto.request.PricingPlanFilterRequest;
import com.findapi.api.pricing.dto.request.PricingPlanUpdateRequest;
import com.findapi.api.pricing.dto.response.PricingPlanDetailResponse;
import com.findapi.api.pricing.dto.response.PricingPlanResponse;
import com.findapi.api.pricing.service.PricingPlanService;
import com.findapi.api.pricing.controller.swagger.PricingPlanControllerSwagger;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class PricingPlanController implements PricingPlanControllerSwagger {
    private final PricingPlanService pricingPlanService;

    @GetMapping("/api/v1/pricing-plans")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageResponse<PricingPlanResponse>> findAll(
            @ModelAttribute PricingPlanFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(pricingPlanService.findAll(filter, pageable));
    }

    @GetMapping("/api/v1/pricing-plans/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PricingPlanDetailResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(pricingPlanService.findById(id));
    }

    @GetMapping("/api/v1/apis/{apiId}/pricing-plans")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<PricingPlanResponse>> findByApiId(@PathVariable UUID apiId) {
        return ResponseEntity.ok(pricingPlanService.findByApiId(apiId));
    }

    @PostMapping("/api/v1/apis/{apiId}/pricing-plans")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVIDER', 'ROLE_ADMIN')")
    public ResponseEntity<PricingPlanDetailResponse> create(
            @PathVariable UUID apiId,
            @Valid @RequestBody PricingPlanCreateRequest request
    ) {
        PricingPlanDetailResponse response = pricingPlanService.create(apiId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/pricing-plans/%s".formatted(response.getId())))
                .body(response);
    }

    @PutMapping("/api/v1/pricing-plans/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVIDER', 'ROLE_ADMIN')")
    public ResponseEntity<PricingPlanDetailResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody PricingPlanUpdateRequest request
    ) {
        return ResponseEntity.ok(pricingPlanService.update(id, request));
    }

    @DeleteMapping("/api/v1/pricing-plans/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        pricingPlanService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
