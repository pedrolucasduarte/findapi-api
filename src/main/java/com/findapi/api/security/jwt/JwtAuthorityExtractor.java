package com.findapi.api.security.jwt;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;

public class JwtAuthorityExtractor {
    private static final String ROLE_PREFIX = "ROLE_";
    private static final String SCOPE_PREFIX = "SCOPE_";

    public Collection<GrantedAuthority> extract(Jwt jwt) {
        Set<String> authorities = new LinkedHashSet<>();
        authorities.addAll(extractScopeAuthorities(jwt.getClaim("scope")));
        authorities.addAll(extractScopeAuthorities(jwt.getClaim("scp")));
        authorities.addAll(extractRoleAuthorities(jwt.getClaim("roles")));
        authorities.addAll(extractDirectAuthorities(jwt.getClaim("authorities")));
        return authorities.stream()
                .map(SimpleGrantedAuthority::new)
                .map(GrantedAuthority.class::cast)
                .toList();
    }

    private Set<String> extractScopeAuthorities(Object claim) {
        return claimValues(claim)
                .map(value -> prefixIfMissing(value, SCOPE_PREFIX))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> extractRoleAuthorities(Object claim) {
        return claimValues(claim)
                .map(value -> prefixIfMissing(value, ROLE_PREFIX))
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private Set<String> extractDirectAuthorities(Object claim) {
        return claimValues(claim)
                .collect(java.util.stream.Collectors.toCollection(LinkedHashSet::new));
    }

    private Stream<String> claimValues(Object claim) {
        if (claim instanceof String value) {
            return split(value);
        }

        if (claim instanceof Collection<?> values) {
            return values.stream()
                    .filter(Objects::nonNull)
                    .flatMap(value -> split(value.toString()));
        }

        return Stream.empty();
    }

    private Stream<String> split(String value) {
        if (value == null || value.isBlank()) {
            return Stream.empty();
        }
        return List.of(value.trim().split("\\s+")).stream()
                .map(String::trim)
                .filter(part -> !part.isBlank());
    }

    private String prefixIfMissing(String authority, String prefix) {
        if (authority.startsWith(prefix)) {
            return authority;
        }
        return prefix + authority;
    }
}
