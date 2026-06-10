package com.findapi.api.authenticationMethod.controller;

import java.net.URI;
import java.util.UUID;

import com.findapi.api.authenticationMethod.dto.request.AuthenticationMethodCreateRequest;
import com.findapi.api.authenticationMethod.dto.request.AuthenticationMethodFilterRequest;
import com.findapi.api.authenticationMethod.dto.request.AuthenticationMethodUpdateRequest;
import com.findapi.api.authenticationMethod.dto.response.AuthenticationMethodDetailResponse;
import com.findapi.api.authenticationMethod.dto.response.AuthenticationMethodResponse;
import com.findapi.api.authenticationMethod.service.AuthenticationMethodService;
import com.findapi.api.authenticationMethod.controller.swagger.AuthenticationMethodControllerSwagger;
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
@RequestMapping("/api/v1/authentication-methods")
@RequiredArgsConstructor
public class AuthenticationMethodController implements AuthenticationMethodControllerSwagger {
    private final AuthenticationMethodService authenticationMethodService;

    @PostMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AuthenticationMethodDetailResponse> create(
            @Valid @RequestBody AuthenticationMethodCreateRequest request
    ) {
        AuthenticationMethodDetailResponse response = authenticationMethodService.create(request);
        return ResponseEntity
                .created(URI.create("/api/v1/authentication-methods/%s".formatted(response.getId())))
                .body(response);
    }

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageResponse<AuthenticationMethodResponse>> findAll(
            @ModelAttribute AuthenticationMethodFilterRequest filter,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(authenticationMethodService.findAll(filter, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthenticationMethodDetailResponse> findById(@PathVariable UUID id) {
        return ResponseEntity.ok(authenticationMethodService.findById(id));
    }

    @GetMapping("/name/{name}")
    @PreAuthorize("permitAll()")
    public ResponseEntity<AuthenticationMethodDetailResponse> findByName(@PathVariable String name) {
        return ResponseEntity.ok(authenticationMethodService.findByName(name));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<AuthenticationMethodDetailResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody AuthenticationMethodUpdateRequest request
    ) {
        return ResponseEntity.ok(authenticationMethodService.update(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable UUID id) {
        authenticationMethodService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
