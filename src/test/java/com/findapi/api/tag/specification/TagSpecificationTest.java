package com.findapi.api.tag.specification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.findapi.api.TestcontainersConfiguration;
import com.findapi.api.entity.TagEntity;
import com.findapi.api.tag.dto.request.TagFilterRequest;
import com.findapi.api.tag.repository.TagRepository;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class TagSpecificationTest {
    @Autowired
    private TagRepository tagRepository;

    @Test
    void filtersByNameAndIgnoresDeletedTags() {
        TagEntity active = saveTag("Spring Boot", "spring-boot");
        TagEntity deleted = saveTag("Spring Legacy", "spring-legacy");
        deleted.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        tagRepository.saveAndFlush(deleted);

        TagFilterRequest filter = TagFilterRequest.builder()
                .name(" spring ")
                .build();

        List<TagEntity> result = tagRepository.findAll(TagSpecification.fromFilter(filter));

        assertThat(result).extracting(TagEntity::getId).containsExactly(active.getId());
    }

    @Test
    void filtersByLowercaseSlug() {
        TagEntity target = saveTag("Spring Boot", "spring-boot");
        saveTag("OAuth2", "oauth2");

        TagFilterRequest filter = TagFilterRequest.builder()
                .slug(" SPRING-BOOT ")
                .build();

        List<TagEntity> result = tagRepository.findAll(TagSpecification.fromFilter(filter));

        assertThat(result).extracting(TagEntity::getId).containsExactly(target.getId());
    }

    @Test
    void alwaysAppliesDeletedAtIsNull() {
        TagEntity active = saveTag("PostgreSQL", "postgresql");
        TagEntity deleted = saveTag("PostgreSQL Legacy", "postgresql-legacy");
        deleted.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        tagRepository.saveAndFlush(deleted);

        List<TagEntity> result = tagRepository.findAll(TagSpecification.fromFilter(null));

        assertThat(result).extracting(TagEntity::getId).containsExactly(active.getId());
    }

    private TagEntity saveTag(String name, String slug) {
        TagEntity entity = new TagEntity();
        entity.setName(name);
        entity.setSlug(slug);
        return tagRepository.saveAndFlush(entity);
    }
}
