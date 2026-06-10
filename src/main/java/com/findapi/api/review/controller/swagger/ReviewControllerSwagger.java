package com.findapi.api.review.controller.swagger;

import java.util.List;
import java.util.UUID;

import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.review.dto.request.ReviewCreateRequest;
import com.findapi.api.review.dto.request.ReviewFilterRequest;
import com.findapi.api.review.dto.request.ReviewUpdateRequest;
import com.findapi.api.review.dto.response.ReviewDetailResponse;
import com.findapi.api.review.dto.response.ReviewResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Reviews", description = "API reviews, ownership checks and soft deletion.")
public interface ReviewControllerSwagger {
    @Operation(summary = "List reviews", description = "Supports filters, page, size and sort.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid filter"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<PageResponse<ReviewResponse>> findAll(
            @ParameterObject ReviewFilterRequest filter, @ParameterObject Pageable pageable);

    @Operation(summary = "Find review by UUID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid UUID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<ReviewDetailResponse> findById(@Parameter(description = "Review UUID") UUID id);

    @Operation(summary = "List active reviews for an API")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Reviews returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<List<ReviewResponse>> findByApiId(@Parameter(description = "API UUID") UUID apiId);

    @Operation(summary = "Review an API",
            description = "Requires USER, PROVIDER or ADMIN. One active review per user and API.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Review created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API or user not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "User already reviewed the API"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<ReviewDetailResponse> create(
            @Parameter(description = "API UUID") UUID apiId,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true) ReviewCreateRequest request);

    @Operation(summary = "Update own review", description = "Requires authentication; ADMIN may update any review.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Review updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not owner or ADMIN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<ReviewDetailResponse> update(UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true) ReviewUpdateRequest request);

    @Operation(summary = "Soft delete own review", description = "Requires authentication; ADMIN may delete any review.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Review soft deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not owner or ADMIN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Review not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<Void> delete(@Parameter(description = "Review UUID") UUID id);
}
