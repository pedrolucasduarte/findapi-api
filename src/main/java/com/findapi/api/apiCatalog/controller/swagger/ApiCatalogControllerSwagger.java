package com.findapi.api.apiCatalog.controller.swagger;

import java.util.UUID;

import com.findapi.api.apiCatalog.dto.request.ApiCreateRequest;
import com.findapi.api.apiCatalog.dto.request.ApiFilterRequest;
import com.findapi.api.apiCatalog.dto.request.ApiUpdateRequest;
import com.findapi.api.apiCatalog.dto.response.ApiDetailResponse;
import com.findapi.api.apiCatalog.dto.response.ApiResponse;
import com.findapi.api.common.pagination.PageResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Tag(name = "API Catalog", description = "Catalog management, discovery and soft deletion of APIs.")
@RequestMapping("/api/v1/apis")
public interface ApiCatalogControllerSwagger {
    @PostMapping
    @Operation(summary = "Create an API", description = "Requires PROVIDER or ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "API created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Slug conflict"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<ApiDetailResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true) ApiCreateRequest request);

    @GetMapping("/{id}")
    @Operation(summary = "Find an API by UUID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "API found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid UUID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<ApiDetailResponse> findById(
            @Parameter(description = "API UUID", required = true, in = ParameterIn.PATH) UUID id);

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Find an API by slug")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "API found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<ApiDetailResponse> findBySlug(
            @Parameter(description = "Normalized API slug", required = true, in = ParameterIn.PATH) String slug);

    @GetMapping
    @Operation(summary = "List and filter active APIs",
            description = "Supports filters, page, size and sort. Maximum page size is 100.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid filter or pagination"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<PageResponse<ApiResponse>> list(
            @ParameterObject ApiFilterRequest filter,
            @ParameterObject Pageable pageable);

    @PutMapping("/{id}")
    @Operation(summary = "Update an API", description = "Requires PROVIDER or ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "API updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Insufficient role"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Slug conflict"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<ApiDetailResponse> update(
            @Parameter(description = "API UUID", required = true) UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true) ApiUpdateRequest request);

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete an API", description = "Sets deletedAt. Requires ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "API soft deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid UUID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "API not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<Void> delete(@Parameter(description = "API UUID", required = true) UUID id);
}
