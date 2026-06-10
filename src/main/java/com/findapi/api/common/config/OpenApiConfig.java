package com.findapi.api.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    private static final String BEARER_AUTH = "bearerAuth";

    @Bean
    OpenAPI findApiOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("FindApi API")
                        .version("0.0.1-SNAPSHOT")
                        .description("""
                                REST API for cataloging, searching, comparing, ranking and reviewing APIs.

                                Roles:
                                - ADMIN: manages the catalog and administrative resources.
                                - PROVIDER: creates and maintains APIs and pricing plans.
                                - USER: creates reviews and collections.
                                - REVIEWER: recognized documentation role for review-focused clients; current write
                                  authorization uses USER, PROVIDER or ADMIN.
                                """)
                        .contact(new Contact()
                                .name("Pedro Duarte")
                                .url("https://github.com/devpedroduarte"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/license/mit")))
                .components(new Components().addSecuritySchemes(
                        BEARER_AUTH,
                        new SecurityScheme()
                                .name(BEARER_AUTH)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")
                                .description("JWT bearer token. Required roles are documented per operation.")
                ));
    }
}
