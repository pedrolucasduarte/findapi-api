package com.findapi.api.category.controller;

import java.net.URI;
import java.util.UUID;

import com.findapi.api.category.dto.request.CategoryCreateRequest;
import com.findapi.api.category.dto.request.CategoryFilterRequest;
import com.findapi.api.category.dto.request.CategoryUpdateRequest;
import com.findapi.api.category.dto.response.CategoryDetailResponse;
import com.findapi.api.category.dto.response.CategoryResponse;
import com.findapi.api.category.service.CategoryService;
import com.findapi.api.category.controller.swagger.CategoryControllerSwagger;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController implements CategoryControllerSwagger {
    private final CategoryService categoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CategoryDetailResponse> create(@Valid @RequestBody CategoryCreateRequest request) {
        CategoryDetailResponse response = categoryService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/categories/%s".formatted(response.getId())))
                .body(response);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageResponse<CategoryResponse>> findAll(
            @ModelAttribute CategoryFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(categoryService.findAll(filter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<CategoryDetailResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(categoryService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<CategoryDetailResponse> findBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(categoryService.findBySlug(slug));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<CategoryDetailResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody CategoryUpdateRequest request
    ) {
        return ResponseEntity.ok(categoryService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
