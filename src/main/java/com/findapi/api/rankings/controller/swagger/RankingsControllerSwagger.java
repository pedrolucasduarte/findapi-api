package com.findapi.api.rankings.controller.swagger;

import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.rankings.dto.response.RankingApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

@Tag(name = "Rankings", description = "Cached public rankings with rating aggregates.")
public interface RankingsControllerSwagger {
    @Operation(summary = "List top-rated APIs")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ranking returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pagination"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<PageResponse<RankingApiResponse>> topRated(@ParameterObject Pageable pageable);

    @Operation(summary = "List APIs with a free tier")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ranking returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pagination"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<PageResponse<RankingApiResponse>> free(@ParameterObject Pageable pageable);

    @Operation(summary = "List open-source APIs")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ranking returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pagination"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<PageResponse<RankingApiResponse>> openSource(@ParameterObject Pageable pageable);

    @Operation(summary = "List Brazilian APIs")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ranking returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pagination"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<PageResponse<RankingApiResponse>> brazilian(@ParameterObject Pageable pageable);

    @Operation(summary = "List newest APIs")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Ranking returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid pagination"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<PageResponse<RankingApiResponse>> newest(@ParameterObject Pageable pageable);
}
