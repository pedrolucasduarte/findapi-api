package com.findapi.api.rankings.dto.response;

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
public class RankingApiResponse {
    private ApiResponse api;
    private Double ratingAverage;
    private long ratingCount;
}
