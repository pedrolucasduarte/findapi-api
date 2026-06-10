package com.findapi.api.entity;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.findapi.api.enums.ApiStatus;
import com.findapi.api.enums.ApiType;
import com.findapi.api.enums.IntegrationDifficulty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "apis")
@Getter
@Setter
@NoArgsConstructor
public class ApiEntity {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "authentication_method_id", nullable = false)
    private AuthenticationMethodEntity authenticationMethod;

    @Column(nullable = false, length = 160)
    private String name;

    @Column(nullable = false, length = 180)
    private String slug;

    @Column(name = "short_description", nullable = false, length = 280)
    private String shortDescription;

    @Column(name = "full_description", columnDefinition = "text")
    private String fullDescription;

    @Column(name = "official_site", length = 2048)
    private String officialSite;

    @Column(name = "documentation_url", length = 2048)
    private String documentationUrl;

    @Enumerated(EnumType.STRING)
    @Column(name = "api_type", nullable = false, length = 30)
    private ApiType apiType;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private ApiStatus status;

    @Column(name = "free_tier", nullable = false)
    private boolean freeTier;

    @Column(name = "official_sdk", nullable = false)
    private boolean officialSdk;

    @Column(name = "open_source", nullable = false)
    private boolean openSource;

    @Column(name = "self_hosted", nullable = false)
    private boolean selfHosted;

    @Column(nullable = false)
    private boolean brazilian;

    @Enumerated(EnumType.STRING)
    @Column(name = "integration_difficulty", nullable = false, length = 30)
    private IntegrationDifficulty integrationDifficulty;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false, updatable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    public void markDeleted(java.time.Instant deletedAt) {
        this.deletedAt = deletedAt.atOffset(java.time.ZoneOffset.UTC);
    }
}
