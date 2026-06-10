package com.findapi.api.tag.controller;

import java.net.URI;
import java.util.UUID;

import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.tag.dto.request.TagCreateRequest;
import com.findapi.api.tag.dto.request.TagFilterRequest;
import com.findapi.api.tag.dto.request.TagUpdateRequest;
import com.findapi.api.tag.dto.response.TagDetailResponse;
import com.findapi.api.tag.dto.response.TagResponse;
import com.findapi.api.tag.service.TagService;
import com.findapi.api.tag.controller.swagger.TagControllerSwagger;

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
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController implements TagControllerSwagger {
    private final TagService tagService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TagDetailResponse> create(@Valid @RequestBody TagCreateRequest request) {
        TagDetailResponse response = tagService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/tags/%s".formatted(response.getId())))
                .body(response);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageResponse<TagResponse>> findAll(
            @ModelAttribute TagFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(tagService.findAll(filter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<TagDetailResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(tagService.findById(id));
    }

    @GetMapping("/slug/{slug}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<TagDetailResponse> findBySlug(@PathVariable String slug) {
        return ResponseEntity.ok(tagService.findBySlug(slug));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<TagDetailResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody TagUpdateRequest request
    ) {
        return ResponseEntity.ok(tagService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        tagService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
