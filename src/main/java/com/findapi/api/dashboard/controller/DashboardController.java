package com.findapi.api.dashboard.controller;

import com.findapi.api.dashboard.dto.response.DashboardResponse;
import com.findapi.api.dashboard.service.DashboardService;
import com.findapi.api.dashboard.controller.swagger.DashboardControllerSwagger;

import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
public class DashboardController implements DashboardControllerSwagger {
    private final DashboardService dashboardService;

    @GetMapping
    @PreAuthorize("permitAll()")
    public ResponseEntity<DashboardResponse> getDashboard() {
        return ResponseEntity.ok(dashboardService.getDashboard());
    }
}
