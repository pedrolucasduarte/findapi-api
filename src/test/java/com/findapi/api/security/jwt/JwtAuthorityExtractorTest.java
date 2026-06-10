package com.findapi.api.security.jwt;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

class JwtAuthorityExtractorTest {
    private final JwtAuthorityExtractor extractor = new JwtAuthorityExtractor();

    @Test
    void extractRolesWithRolePrefix() {
        Jwt jwt = jwt(Map.of("roles", List.of("ADMIN", "ROLE_USER")));

        assertThat(authorities(jwt)).containsExactly("ROLE_ADMIN", "ROLE_USER");
    }

    @Test
    void extractDirectAuthorities() {
        Jwt jwt = jwt(Map.of("authorities", List.of("API_READ", "API_WRITE")));

        assertThat(authorities(jwt)).containsExactly("API_READ", "API_WRITE");
    }

    @Test
    void extractScopeAsSpaceSeparatedString() {
        Jwt jwt = jwt(Map.of("scope", "api.read api.write"));

        assertThat(authorities(jwt)).containsExactly("SCOPE_api.read", "SCOPE_api.write");
    }

    @Test
    void extractScpAsList() {
        Jwt jwt = jwt(Map.of("scp", List.of("catalog.read", "catalog.write")));

        assertThat(authorities(jwt)).containsExactly("SCOPE_catalog.read", "SCOPE_catalog.write");
    }

    @Test
    void ignoreMissingClaims() {
        Jwt jwt = jwt(Map.of("sub", "user-1"));

        assertThat(authorities(jwt)).isEmpty();
    }

    @Test
    void removeDuplicatedAuthorities() {
        Jwt jwt = jwt(Map.of(
                "roles", List.of("ADMIN", "ROLE_ADMIN"),
                "authorities", List.of("API_READ", "API_READ"),
                "scope", "api.read api.read"
        ));

        assertThat(authorities(jwt)).containsExactly("SCOPE_api.read", "ROLE_ADMIN", "API_READ");
    }

    private List<String> authorities(Jwt jwt) {
        return extractor.extract(jwt)
                .stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
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
