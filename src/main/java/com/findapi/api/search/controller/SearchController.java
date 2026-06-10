package com.findapi.api.search.controller;

import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.search.dto.request.ApiSearchRequest;
import com.findapi.api.search.dto.response.ApiSearchResponse;
import com.findapi.api.search.service.SearchService;
import com.findapi.api.search.controller.swagger.SearchControllerSwagger;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController implements SearchControllerSwagger {
    private final SearchService searchService;

    @GetMapping("/apis")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageResponse<ApiSearchResponse>> searchApis(
            @ModelAttribute ApiSearchRequest request,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return ResponseEntity.ok(searchService.search(request, pageable));
    }
}
