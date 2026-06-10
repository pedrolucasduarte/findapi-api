package com.findapi.api.apiCatalog.controller;

import java.util.List;
import java.util.UUID;

import com.findapi.api.apiCatalog.service.ApiRelationshipService;
import com.findapi.api.apiCatalog.controller.swagger.ApiRelationshipControllerSwagger;
import com.findapi.api.category.dto.response.CategoryResponse;
import com.findapi.api.tag.dto.response.TagResponse;

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
public class ApiRelationshipController implements ApiRelationshipControllerSwagger {
    private final ApiRelationshipService apiRelationshipService;

    @PostMapping("/api/v1/apis/{apiId}/categories/{categoryId}")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVIDER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> addCategory(@PathVariable UUID apiId, @PathVariable UUID categoryId) {
        apiRelationshipService.addCategory(apiId, categoryId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/v1/apis/{apiId}/categories/{categoryId}")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVIDER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> removeCategory(@PathVariable UUID apiId, @PathVariable UUID categoryId) {
        apiRelationshipService.removeCategory(apiId, categoryId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/apis/{apiId}/categories")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<CategoryResponse>> findCategories(@PathVariable UUID apiId) {
        return ResponseEntity.ok(apiRelationshipService.findCategories(apiId));
    }

    @PostMapping("/api/v1/apis/{apiId}/tags/{tagId}")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVIDER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> addTag(@PathVariable UUID apiId, @PathVariable UUID tagId) {
        apiRelationshipService.addTag(apiId, tagId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/api/v1/apis/{apiId}/tags/{tagId}")
    @PreAuthorize("hasAnyAuthority('ROLE_PROVIDER', 'ROLE_ADMIN')")
    public ResponseEntity<Void> removeTag(@PathVariable UUID apiId, @PathVariable UUID tagId) {
        apiRelationshipService.removeTag(apiId, tagId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/v1/apis/{apiId}/tags")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<TagResponse>> findTags(@PathVariable UUID apiId) {
        return ResponseEntity.ok(apiRelationshipService.findTags(apiId));
    }
}
