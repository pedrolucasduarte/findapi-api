package com.findapi.api.rankings.controller;

import com.findapi.api.common.pagination.PageResponse;
import com.findapi.api.rankings.dto.response.RankingApiResponse;
import com.findapi.api.rankings.service.RankingsService;
import com.findapi.api.rankings.controller.swagger.RankingsControllerSwagger;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/rankings")
@RequiredArgsConstructor
public class RankingsController implements RankingsControllerSwagger {
    private final RankingsService rankingsService;

    @GetMapping("/top-rated")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageResponse<RankingApiResponse>> topRated(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(rankingsService.topRated(pageable));
    }

    @GetMapping("/free")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageResponse<RankingApiResponse>> free(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(rankingsService.free(pageable));
    }

    @GetMapping("/open-source")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageResponse<RankingApiResponse>> openSource(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(rankingsService.openSource(pageable));
    }

    @GetMapping("/brazilian")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageResponse<RankingApiResponse>> brazilian(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(rankingsService.brazilian(pageable));
    }

    @GetMapping("/newest")
    @PreAuthorize("permitAll()")
    public ResponseEntity<PageResponse<RankingApiResponse>> newest(
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(rankingsService.newest(pageable));
    }
}
