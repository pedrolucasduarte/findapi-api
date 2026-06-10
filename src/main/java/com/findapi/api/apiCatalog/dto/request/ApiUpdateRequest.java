package com.findapi.api.apiCatalog.dto.request;

import java.util.UUID;

import com.findapi.api.enums.ApiStatus;
import com.findapi.api.enums.ApiType;
import com.findapi.api.enums.IntegrationDifficulty;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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
public class ApiUpdateRequest {
    @NotBlank
    @Size(max = 160)
    private String name;

    @NotBlank
    @Size(max = 180)
    @Pattern(regexp = "^[A-Za-z0-9]+(-[A-Za-z0-9]+)*$")
    private String slug;

    @NotBlank
    @Size(max = 280)
    private String shortDescription;

    @Size(max = 20000)
    private String fullDescription;

    @Pattern(regexp = "^https?://.+$")
    @Size(max = 2048)
    private String officialSite;

    @Pattern(regexp = "^https?://.+$")
    @Size(max = 2048)
    private String documentationUrl;

    @NotNull
    private ApiType apiType;

    @NotNull
    private ApiStatus status;

    private boolean freeTier;
    private boolean officialSdk;
    private boolean openSource;
    private boolean selfHosted;
    private boolean brazilian;

    @NotNull
    private IntegrationDifficulty integrationDifficulty;

    @NotNull
    private UUID authenticationMethodId;
}
