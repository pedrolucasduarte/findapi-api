package com.findapi.api.search.controller.swagger;

import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.search.dto.request.ApiSearchRequest;
import com.findapi.api.search.dto.response.ApiSearchResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Search", description = "Advanced public API discovery.")
public interface SearchControllerSwagger {
    @Operation(summary = "Search APIs",
            description = "Combines name, categoryId, tagId, type, authentication method, flags, difficulty and status. "
                    + "Supports page, size and sort; maximum size is 100.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Search page returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid filter, UUID, enum or sort"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<PageResponse<ApiSearchResponse>> searchApis(
            @ParameterObject ApiSearchRequest request, @ParameterObject Pageable pageable);
}
