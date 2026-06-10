package com.findapi.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc
class OpenApiContractTest {
    @Autowired
    private MockMvc mockMvc;

    @Test
    void exposesDocumentedModulesAndJwtScheme() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("FindApi API"))
                .andExpect(jsonPath("$.info.license.name").value("MIT"))
                .andExpect(jsonPath("$.components.securitySchemes.bearerAuth.scheme").value("bearer"))
                .andExpect(jsonPath("$.paths['/api/v1/apis']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/categories']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/tags']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/authentication-methods']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/pricing-plans']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/reviews']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/collections']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/search/apis']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/dashboard']").exists())
                .andExpect(jsonPath("$.paths['/api/v1/rankings/top-rated']").exists());
    }

    @Test
    void appliesSecurityOnlyToProtectedOperations() throws Exception {
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/v1/apis'].get.security").doesNotExist())
                .andExpect(jsonPath("$.paths['/api/v1/apis'].post.security[0].bearerAuth").isArray())
                .andExpect(jsonPath("$.paths['/api/v1/users/me'].get.security[0].bearerAuth").isArray());
    }
}
