package com.findapi.api.search.dto.response;

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
public class ApiSearchResponse {
    private UUID id;
    private String name;
    private String slug;
    private String shortDescription;
    private ApiType apiType;
    private ApiStatus status;
    private boolean freeTier;
    private boolean officialSdk;
    private boolean openSource;
    private boolean selfHosted;
    private boolean brazilian;
    private IntegrationDifficulty integrationDifficulty;
    private UUID authenticationMethodId;
    private Instant createdAt;
    private Instant updatedAt;
}
