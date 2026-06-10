package com.findapi.api.security.config;

import com.findapi.api.security.jwt.JwtProperties;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@EnableConfigurationProperties(JwtProperties.class)
public class SecurityConfig {
    private static final String[] PUBLIC_ENDPOINTS = {
            "/actuator/health/**",
            "/actuator/info",
            "/swagger-ui/**",
            "/v3/api-docs/**",
            "/swagger-ui.html"
    };

    @Bean
    SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtProperties jwtProperties,
            JwtAuthenticationConverter jwtAuthenticationConverter
    ) throws Exception {
        http.csrf(AbstractHttpConfigurer::disable);
        http.cors(Customizer.withDefaults());
        http.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        http.authorizeHttpRequests(authorize -> {
            authorize.requestMatchers(PUBLIC_ENDPOINTS).permitAll();
            authorize.requestMatchers("/actuator/metrics/**").hasAuthority("ROLE_ADMIN");
            authorize.requestMatchers(HttpMethod.GET, "/api/v1/apis/**").permitAll();
            authorize.requestMatchers(HttpMethod.GET, "/api/v1/categories/**").permitAll();
            authorize.requestMatchers(HttpMethod.GET, "/api/v1/tags/**").permitAll();
            authorize.requestMatchers(HttpMethod.GET, "/api/v1/authentication-methods/**").permitAll();
            authorize.requestMatchers(HttpMethod.GET, "/api/v1/pricing-plans/**").permitAll();
            authorize.requestMatchers(HttpMethod.GET, "/api/v1/reviews/**").permitAll();
            authorize.requestMatchers(HttpMethod.GET, "/api/v1/collections/**").permitAll();
            authorize.requestMatchers(HttpMethod.GET, "/api/v1/search/**").permitAll();
            authorize.requestMatchers(HttpMethod.GET, "/api/v1/dashboard").permitAll();
            authorize.requestMatchers(HttpMethod.GET, "/api/v1/rankings/**").permitAll();
            if (jwtProperties.isConfigured()) {
                authorize.anyRequest().authenticated();
            } else {
                authorize.anyRequest().denyAll();
            }
        });

        if (jwtProperties.isConfigured()) {
            http.oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> jwt
                    .decoder(jwtDecoder(jwtProperties))
                    .jwtAuthenticationConverter(jwtAuthenticationConverter)));
        }

        return http.build();
    }

    private JwtDecoder jwtDecoder(JwtProperties jwtProperties) {
        if (hasText(jwtProperties.getJwkSetUri())) {
            return NimbusJwtDecoder.withJwkSetUri(jwtProperties.getJwkSetUri()).build();
        }
        return JwtDecoders.fromIssuerLocation(jwtProperties.getIssuerUri());
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
