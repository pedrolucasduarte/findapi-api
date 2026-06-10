package com.findapi.api.review.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.review.dto.request.ReviewCreateRequest;
import com.findapi.api.review.dto.request.ReviewFilterRequest;
import com.findapi.api.review.dto.request.ReviewUpdateRequest;
import com.findapi.api.review.dto.response.ReviewDetailResponse;
import com.findapi.api.review.dto.response.ReviewResponse;
import com.findapi.api.review.service.ReviewService;
import com.findapi.api.review.controller.swagger.ReviewControllerSwagger;

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
public class ReviewController implements ReviewControllerSwagger {
    private final ReviewService reviewService;

    @GetMapping("/api/v1/reviews")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageResponse<ReviewResponse>> findAll(
            @ModelAttribute ReviewFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(reviewService.findAll(filter, pageable));
    }

    @GetMapping("/api/v1/reviews/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<ReviewDetailResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(reviewService.findById(id));
    }

    @GetMapping("/api/v1/apis/{apiId}/reviews")
    @PreAuthorize("permitAll()")
    public ResponseEntity<List<ReviewResponse>> findByApiId(@PathVariable UUID apiId) {
        return ResponseEntity.ok(reviewService.findByApiId(apiId));
    }

    @PostMapping("/api/v1/apis/{apiId}/reviews")
    @PreAuthorize("hasAnyAuthority('ROLE_USER', 'ROLE_PROVIDER', 'ROLE_ADMIN')")
    public ResponseEntity<ReviewDetailResponse> create(
            @PathVariable UUID apiId,
            @Valid @RequestBody ReviewCreateRequest request
    ) {
        ReviewDetailResponse response = reviewService.create(apiId, request);
        return ResponseEntity
                .created(URI.create("/api/v1/reviews/%s".formatted(response.getId())))
                .body(response);
    }

    @PutMapping("/api/v1/reviews/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewDetailResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody ReviewUpdateRequest request
    ) {
        return ResponseEntity.ok(reviewService.update(id, request));
    }

    @DeleteMapping("/api/v1/reviews/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        reviewService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
