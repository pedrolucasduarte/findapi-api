package com.findapi.api.security.jwt;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;

@Configuration
public class JwtAuthenticationConverterConfig {
    @Bean
    JwtAuthorityExtractor jwtAuthorityExtractor() {
        return new JwtAuthorityExtractor();
    }

    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter(JwtAuthorityExtractor jwtAuthorityExtractor) {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwtAuthorityExtractor::extract);
        return converter;
    }
}
