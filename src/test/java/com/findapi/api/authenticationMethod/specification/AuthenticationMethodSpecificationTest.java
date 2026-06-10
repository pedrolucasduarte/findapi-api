package com.findapi.api.authenticationMethod.specification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.findapi.api.TestcontainersConfiguration;
import com.findapi.api.authenticationMethod.dto.request.AuthenticationMethodFilterRequest;
import com.findapi.api.authenticationMethod.repository.AuthenticationMethodRepository;
import com.findapi.api.entity.AuthenticationMethodEntity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class AuthenticationMethodSpecificationTest {
    @Autowired
    private AuthenticationMethodRepository authenticationMethodRepository;

    @Test
    void filtersByName() {
        AuthenticationMethodFilterRequest filter = AuthenticationMethodFilterRequest.builder()
                .name(" api ")
                .build();

        List<AuthenticationMethodEntity> result = authenticationMethodRepository
                .findAll(AuthenticationMethodSpecification.fromFilter(filter));

        assertThat(result).extracting(AuthenticationMethodEntity::getName).containsExactly("API_KEY");
    }

    @Test
    void alwaysAppliesDeletedAtIsNull() {
        AuthenticationMethodEntity hmac = authenticationMethodRepository
                .findByNameAndDeletedAtIsNull("HMAC")
                .orElseThrow();
        hmac.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        authenticationMethodRepository.saveAndFlush(hmac);

        List<AuthenticationMethodEntity> result = authenticationMethodRepository
                .findAll(AuthenticationMethodSpecification.fromFilter(null));

        assertThat(result).extracting(AuthenticationMethodEntity::getName)
                .contains("NONE", "API_KEY", "BEARER", "OAUTH2", "BASIC_AUTH")
                .doesNotContain("HMAC");
    }
}
