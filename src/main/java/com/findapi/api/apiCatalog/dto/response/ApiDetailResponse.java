package com.findapi.api.apiCatalog.dto.response;

import java.time.Instant;
import java.util.UUID;

import com.findapi.api.enums.ApiStatus;
import com.findapi.api.enums.ApiType;
import com.findapi.api.enums.IntegrationDifficulty;

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
public class ApiDetailResponse {
    private UUID id;
    private String name;
    private String slug;
    private String shortDescription;
    private String fullDescription;
    private String officialSite;
    private String documentationUrl;
    private ApiType apiType;
    private ApiStatus status;
    private boolean freeTier;
    private boolean officialSdk;
    private boolean openSource;
    private boolean selfHosted;
    private boolean brazilian;
    private IntegrationDifficulty integrationDifficulty;
    private UUID authenticationMethodId;
    private String authenticationMethodName;
    private Double ratingAverage;
    private long ratingCount;
    private long oneStar;
    private long twoStars;
    private long threeStars;
    private long fourStars;
    private long fiveStars;
    private Instant createdAt;
    private Instant updatedAt;
}
