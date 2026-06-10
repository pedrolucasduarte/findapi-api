package com.findapi.api.pricing.controller.swagger;

import java.util.List;
import java.util.UUID;

import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.pricing.dto.request.PricingPlanCreateRequest;
import com.findapi.api.pricing.dto.request.PricingPlanFilterRequest;
import com.findapi.api.pricing.dto.request.PricingPlanUpdateRequest;
import com.findapi.api.pricing.dto.response.PricingPlanDetailResponse;
import com.findapi.api.pricing.dto.response.PricingPlanResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Pricing Plans", description = "API pricing plans with filtering and soft delete.")
public interface PricingPlanControllerSwagger {
    @Operation(summary = "List pricing plans", description = "Supports filters, page, size and sort.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid filter"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<PageResponse<PricingPlanResponse>> findAll(
            @ParameterObject PricingPlanFilterRequest filter, @ParameterObject Pageable pageable);

    @Operation(summary = "Find pricing plan by UUID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plan found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid UUID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Plan not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<PricingPlanDetailResponse> findById(@Parameter(description = "Pricing plan UUID") UUID id);

    @Operation(summary = "List active pricing plans for an API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plans returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<List<PricingPlanResponse>> findByApiId(@Parameter(description = "API UUID") UUID apiId);

    @Operation(summary = "Create a pricing plan for an API", description = "Requires PROVIDER or ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Plan created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Data conflict"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<PricingPlanDetailResponse> create(
            @Parameter(description = "API UUID") UUID apiId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true) PricingPlanCreateRequest request);

    @Operation(summary = "Update a pricing plan", description = "Requires PROVIDER or ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Plan updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Plan not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Data conflict"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<PricingPlanDetailResponse> update(
            @Parameter(description = "Pricing plan UUID") UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true) PricingPlanUpdateRequest request);

    @Operation(summary = "Soft delete a pricing plan", description = "Requires ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Plan soft deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Plan not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<Void> delete(@Parameter(description = "Pricing plan UUID") UUID id);
}
