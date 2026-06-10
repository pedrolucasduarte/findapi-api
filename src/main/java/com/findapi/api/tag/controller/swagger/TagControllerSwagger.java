package com.findapi.api.tag.controller.swagger;

import java.util.UUID;

import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.tag.dto.request.TagCreateRequest;
import com.findapi.api.tag.dto.request.TagFilterRequest;
import com.findapi.api.tag.dto.request.TagUpdateRequest;
import com.findapi.api.tag.dto.response.TagDetailResponse;
import com.findapi.api.tag.dto.response.TagResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

@Tag(name = "Tags", description = "Tag catalog with filtering and soft delete.")
@RequestMapping("/api/v1/tags")
public interface TagControllerSwagger {
    @PostMapping
    @Operation(summary = "Create a tag", description = "Requires ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Tag created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Slug conflict"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<TagDetailResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true) TagCreateRequest request);

    @GetMapping
    @Operation(summary = "List active tags", description = "Supports filters, page, size and sort.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid filter"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<PageResponse<TagResponse>> findAll(
            @ParameterObject TagFilterRequest filter, @ParameterObject Pageable pageable);

    @GetMapping("/{id}")
    @Operation(summary = "Find tag by UUID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid UUID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<TagDetailResponse> findById(@Parameter(description = "Tag UUID") UUID id);

    @GetMapping("/slug/{slug}")
    @Operation(summary = "Find tag by slug")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<TagDetailResponse> findBySlug(@Parameter(description = "Normalized slug") String slug);

    @PutMapping("/{id}")
    @Operation(summary = "Update a tag", description = "Requires ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Tag updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Slug conflict"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<TagDetailResponse> update(
            @Parameter(description = "Tag UUID") UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true) TagUpdateRequest request);

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete a tag", description = "Requires ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Tag soft deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Tag not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<Void> delete(@Parameter(description = "Tag UUID") UUID id);
}
