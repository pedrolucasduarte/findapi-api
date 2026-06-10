package com.findapi.api.collection.controller;

import java.util.List;
import java.util.UUID;

import com.findapi.api.apiCatalog.dto.response.ApiResponse;
import com.findapi.api.collection.service.CollectionApiService;
import com.findapi.api.collection.controller.swagger.CollectionApiControllerSwagger;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class CollectionApiController implements CollectionApiControllerSwagger {
    private final CollectionApiService collectionApiService;

    @PostMapping("/api/v1/collections/{collectionId}/apis/{apiId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> addApi(@PathVariable UUID collectionId, @PathVariable UUID apiId) {
        collectionApiService.addApi(collectionId, apiId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/v1/collections/{collectionId}/apis/{apiId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> removeApi(@PathVariable UUID collectionId, @PathVariable UUID apiId) {
        collectionApiService.removeApi(collectionId, apiId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/collections/{collectionId}/apis")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ApiResponse>> findApis(@PathVariable UUID collectionId) {
        return ResponseEntity.ok(collectionApiService.findApis(collectionId));
    }
}
