package com.findapi.api.apiCatalog.dto.request;

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
public class ApiFilterRequest {
    private String name;
    private String slug;
    private ApiType apiType;
    private ApiStatus status;
    private Boolean freeTier;
    private Boolean officialSdk;
    private Boolean openSource;
    private Boolean selfHosted;
    private Boolean brazilian;
    private IntegrationDifficulty integrationDifficulty;
    private UUID authenticationMethodId;
}
