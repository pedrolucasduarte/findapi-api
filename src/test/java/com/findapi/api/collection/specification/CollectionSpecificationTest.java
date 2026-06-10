package com.findapi.api.collection.specification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.findapi.api.TestcontainersConfiguration;
import com.findapi.api.collection.dto.request.CollectionFilterRequest;
import com.findapi.api.collection.repository.CollectionRepository;
import com.findapi.api.entity.AppUserEntity;
import com.findapi.api.entity.CollectionEntity;
import com.findapi.api.enums.UserRole;
import com.findapi.api.user.repository.UserRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class CollectionSpecificationTest {
    @Autowired
    private CollectionRepository collectionRepository;

    @Autowired
    private UserRepository userRepository;

    @Test
    void filtersByNameAndIgnoresDeletedCollections() {
        AppUserEntity owner = saveUser("owner-name@example.com");
        CollectionEntity active = saveCollection(owner, "Dev Tools", "dev-tools");
        CollectionEntity deleted = saveCollection(owner, "Dev Legacy", "dev-legacy");
        deleted.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        collectionRepository.saveAndFlush(deleted);

        CollectionFilterRequest filter = CollectionFilterRequest.builder()
                .name(" dev ")
                .build();

        List<CollectionEntity> result = collectionRepository.findAll(CollectionSpecification.fromFilter(filter));

        assertThat(result).extracting(CollectionEntity::getId).containsExactly(active.getId());
    }

    @Test
    void filtersBySlugAndOwnerId() {
        AppUserEntity owner = saveUser("owner-slug@example.com");
        AppUserEntity otherOwner = saveUser("other-owner-slug@example.com");
        CollectionEntity target = saveCollection(owner, "Backend APIs", "backend-apis");
        saveCollection(otherOwner, "Frontend APIs", "frontend-apis");

        CollectionFilterRequest filter = CollectionFilterRequest.builder()
                .slug(" BACKEND-APIS ")
                .ownerId(owner.getId())
                .build();

        List<CollectionEntity> result = collectionRepository.findAll(CollectionSpecification.fromFilter(filter));

        assertThat(result).extracting(CollectionEntity::getId).containsExactly(target.getId());
    }

    @Test
    void alwaysAppliesDeletedAtIsNull() {
        AppUserEntity owner = saveUser("owner-deleted@example.com");
        CollectionEntity active = saveCollection(owner, "Active Collection", "active-collection");
        CollectionEntity deleted = saveCollection(owner, "Deleted Collection", "deleted-collection");
        deleted.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        collectionRepository.saveAndFlush(deleted);

        List<CollectionEntity> result = collectionRepository.findAll(CollectionSpecification.fromFilter(null));

        assertThat(result).extracting(CollectionEntity::getId).contains(active.getId());
        assertThat(result).extracting(CollectionEntity::getId).doesNotContain(deleted.getId());
    }

    private CollectionEntity saveCollection(AppUserEntity owner, String name, String slug) {
        CollectionEntity entity = new CollectionEntity();
        entity.setUser(owner);
        entity.setName(name);
        entity.setSlug(slug);
        entity.setDescription("Useful APIs");
        return collectionRepository.saveAndFlush(entity);
    }

    private AppUserEntity saveUser(String email) {
        AppUserEntity entity = new AppUserEntity();
        entity.setName("Owner");
        entity.setEmail(email);
        entity.setPasswordHash("hash");
        entity.setRole(UserRole.USER);
        return userRepository.saveAndFlush(entity);
    }
}
