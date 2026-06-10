package com.findapi.api.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

class JwtAuthenticationConverterConfigTest {
    @Test
    void createConverterUsingCustomAuthorityExtractor() {
        JwtAuthenticationConverterConfig config = new JwtAuthenticationConverterConfig();
        JwtAuthenticationConverter converter = config.jwtAuthenticationConverter(new JwtAuthorityExtractor());

        var authentication = converter.convert(jwt(Map.of("roles", "ADMIN")));

        assertThat(authentication).isNotNull();
        assertThat(authentication.getAuthorities())
                .extracting("authority")
                .contains("ROLE_ADMIN");
    }

    private Jwt jwt(Map<String, Object> claims) {
        return new Jwt(
                "token",
                Instant.now(),
                Instant.now().plusSeconds(300),
                Map.of("alg", "none"),
                claims
        );
    }
}
