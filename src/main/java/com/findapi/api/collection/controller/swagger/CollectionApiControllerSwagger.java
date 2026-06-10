package com.findapi.api.collection.controller.swagger;

import java.util.List;
import java.util.UUID;

import com.findapi.api.apiCatalog.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Collection APIs", description = "Collection/API relationships controlled by collection ownership or ADMIN.")
public interface CollectionApiControllerSwagger {
    @Operation(summary = "Add an API to a collection",
            description = "Requires authentication and collection ownership or ADMIN. Reactivates a soft-deleted link.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "API added"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid UUID or principal"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not owner or ADMIN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Collection or API not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Active relationship already exists"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<Void> addApi(@Parameter(description = "Collection UUID") UUID collectionId,
            @Parameter(description = "API UUID") UUID apiId);

    @Operation(summary = "Soft delete an API from a collection",
            description = "Requires authentication and collection ownership or ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Relationship soft deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "Not owner or ADMIN"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Relationship not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<Void> removeApi(UUID collectionId, UUID apiId);

    @Operation(summary = "List active APIs in a collection")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "APIs returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Collection not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<List<ApiResponse>> findApis(@Parameter(description = "Collection UUID") UUID collectionId);
}
