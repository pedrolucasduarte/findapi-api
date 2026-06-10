package com.findapi.api.search.dto.request;

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
public class ApiSearchRequest {
    private String name;
    private UUID categoryId;
    private UUID tagId;
    private ApiType apiType;
    private UUID authenticationMethodId;
    private Boolean freeTier;
    private Boolean officialSdk;
    private Boolean openSource;
    private Boolean selfHosted;
    private Boolean brazilian;
    private IntegrationDifficulty integrationDifficulty;
    private ApiStatus status;
}
