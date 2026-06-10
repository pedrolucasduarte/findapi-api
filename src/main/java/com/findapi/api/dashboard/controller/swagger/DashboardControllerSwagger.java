package com.findapi.api.dashboard.controller.swagger;

import com.findapi.api.dashboard.dto.response.DashboardResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;

@Tag(name = "Dashboard", description = "Cached public catalog totals and highlights.")
public interface DashboardControllerSwagger {
    @Operation(summary = "Load dashboard",
            description = "Returns totals plus latest, top-rated and Brazilian API lists.")
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Dashboard returned"),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal error")
    })
    ResponseEntity<DashboardResponse> getDashboard();
}
