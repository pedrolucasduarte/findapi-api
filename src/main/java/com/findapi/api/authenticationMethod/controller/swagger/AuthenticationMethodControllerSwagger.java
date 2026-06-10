package com.findapi.api.authenticationMethod.controller.swagger;

import java.util.UUID;

import com.findapi.api.authenticationMethod.dto.request.AuthenticationMethodCreateRequest;
import com.findapi.api.authenticationMethod.dto.request.AuthenticationMethodFilterRequest;
import com.findapi.api.authenticationMethod.dto.request.AuthenticationMethodUpdateRequest;
import com.findapi.api.authenticationMethod.dto.response.AuthenticationMethodDetailResponse;
import com.findapi.api.authenticationMethod.dto.response.AuthenticationMethodResponse;
import com.findapi.api.common.pagination.PageResponse;
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

@Tag(name = "Authentication Methods", description = "Authentication mechanisms supported by cataloged APIs.")
@RequestMapping("/api/v1/authentication-methods")
public interface AuthenticationMethodControllerSwagger {
    @PostMapping
    @Operation(summary = "Create an authentication method", description = "Requires ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Method created"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Name conflict"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<AuthenticationMethodDetailResponse> create(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true)
            AuthenticationMethodCreateRequest request);

    @GetMapping
    @Operation(summary = "List authentication methods", description = "Supports filters, page, size and sort.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Page returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid filter"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<PageResponse<AuthenticationMethodResponse>> findAll(
            @ParameterObject AuthenticationMethodFilterRequest filter, @ParameterObject Pageable pageable);

    @GetMapping("/{id}")
    @Operation(summary = "Find authentication method by UUID")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Method found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid UUID"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Method not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<AuthenticationMethodDetailResponse> findById(
            @Parameter(description = "Authentication method UUID") UUID id);

    @GetMapping("/name/{name}")
    @Operation(summary = "Find authentication method by name")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Method found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Method not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<AuthenticationMethodDetailResponse> findByName(
            @Parameter(description = "Method name, for example API_KEY") String name);

    @PutMapping("/{id}")
    @Operation(summary = "Update an authentication method", description = "Requires ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Method updated"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid request"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Method not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Name conflict"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<AuthenticationMethodDetailResponse> update(
            @Parameter(description = "Authentication method UUID") UUID id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true)
            AuthenticationMethodUpdateRequest request);

    @DeleteMapping("/{id}")
    @Operation(summary = "Soft delete an authentication method", description = "Requires ADMIN.")
    @SecurityRequirement(name = "bearerAuth")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "204", description = "Method soft deleted"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "JWT required or invalid"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "403", description = "ADMIN required"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Method not found"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "409", description = "Method is referenced"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<Void> delete(@Parameter(description = "Authentication method UUID") UUID id);
}
