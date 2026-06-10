package com.findapi.api.collection.controller;

import java.net.URI;
import java.util.UUID;

import com.findapi.api.collection.dto.request.CollectionCreateRequest;
import com.findapi.api.collection.dto.request.CollectionFilterRequest;
import com.findapi.api.collection.dto.request.CollectionUpdateRequest;
import com.findapi.api.collection.dto.response.CollectionDetailResponse;
import com.findapi.api.collection.dto.response.CollectionResponse;
import com.findapi.api.collection.service.CollectionService;
import com.findapi.api.collection.controller.swagger.CollectionControllerSwagger;
import com.findapi.api.common.pagination.PageResponse;

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
public class CollectionController implements CollectionControllerSwagger {
    private final CollectionService collectionService;

    @GetMapping("/api/v1/collections")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageResponse<CollectionResponse>> findAll(
            @ModelAttribute CollectionFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(collectionService.findAll(filter, pageable));
    }

    @GetMapping("/api/v1/collections/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<CollectionDetailResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(collectionService.findById(id));
    }

    @GetMapping("/api/v1/collections/slug/{slug}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<CollectionDetailResponse> findBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(collectionService.findBySlug(slug));
    }

    @PostMapping("/api/v1/collections")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_PROVIDER', 'ROLE_ADMIN')")
    public ResponseEntity<CollectionDetailResponse> create(@Valid @RequestBody CollectionCreateRequest request) {
        CollectionDetailResponse response = collectionService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/collections/%s".formatted(response.getId())))
                .body(response);
    }

    @PutMapping("/api/v1/collections/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<CollectionDetailResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CollectionUpdateRequest request
    ) {
        return ResponseEntity.ok(collectionService.update(id, request));
    }

    @DeleteMapping("/api/v1/collections/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        collectionService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
