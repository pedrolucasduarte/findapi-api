package com.findapi.api.security.jwt;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "findapi.security.jwt")
public class JwtProperties {
    private String issuerUri;
    private String jwkSetUri;

    public String getIssuerUri() {
        return issuerUri;
    }

    public void setIssuerUri(String issuerUri) {
        this.issuerUri = issuerUri;
    }

    public String getJwkSetUri() {
        return jwkSetUri;
    }

    public void setJwkSetUri(String jwkSetUri) {
        this.jwkSetUri = jwkSetUri;
    }

    public boolean isConfigured() {
        return hasText(issuerUri) || hasText(jwkSetUri);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
