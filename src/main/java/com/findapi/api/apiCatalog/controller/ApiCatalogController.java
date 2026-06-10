package com.findapi.api.apiCatalog.controller;

import java.net.URI;
import java.util.UUID;

import com.findapi.api.apiCatalog.dto.request.ApiCreateRequest;
import com.findapi.api.apiCatalog.dto.request.ApiFilterRequest;
import com.findapi.api.apiCatalog.dto.request.ApiUpdateRequest;
import com.findapi.api.apiCatalog.dto.response.ApiDetailResponse;
import com.findapi.api.apiCatalog.dto.response.ApiResponse;
import com.findapi.api.apiCatalog.service.ApiCatalogService;
import com.findapi.api.apiCatalog.controller.swagger.ApiCatalogControllerSwagger;
import com.findapi.api.common.pagination.PageResponse;

import jakarta.validation.Valid;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/apis")
public class ApiCatalogController implements ApiCatalogControllerSwagger {
    private final ApiCatalogService apiCatalogService;

    public ApiCatalogController(ApiCatalogService apiCatalogService) {
        this.apiCatalogService = apiCatalogService;
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('ROLE_PROVIDER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiDetailResponse> create(@Valid @RequestBody ApiCreateRequest request) {
        ApiDetailResponse response = apiCatalogService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/apis/%s".formatted(response.getId())))
                .body(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiDetailResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(apiCatalogService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ApiDetailResponse> findBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(apiCatalogService.findBySlug(slug));
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageResponse<ApiResponse>> list(
            @ModelAttribute ApiFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(apiCatalogService.list(filter, pageable));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVIDER', 'ROLE_ADMIN')")
    public ResponseEntity<ApiDetailResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ApiUpdateRequest request
    ) {
        return ResponseEntity.ok(apiCatalogService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        apiCatalogService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
