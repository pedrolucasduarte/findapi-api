package com.findapi.api.dashboard.dto.response;

import java.util.List;

import com.findapi.api.apiCatalog.dto.response.ApiResponse;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private long totalApis;
    private long totalCategories;
    private long totalTags;
    private long totalReviews;
    private long totalCollections;
    private List<ApiResponse> latestApis;
    private List<ApiResponse> topRatedApis;
    private List<ApiResponse> brazilianApis;
}
