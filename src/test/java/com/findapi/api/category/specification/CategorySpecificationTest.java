package com.findapi.api.category.specification;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;

import com.findapi.api.TestcontainersConfiguration;
import com.findapi.api.category.dto.request.CategoryFilterRequest;
import com.findapi.api.category.repository.CategoryRepository;
import com.findapi.api.entity.CategoryEntity;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class CategorySpecificationTest {
    @Autowired
    private CategoryRepository categoryRepository;

    @Test
    void filtersByNameAndIgnoresDeletedCategories() {
        CategoryEntity active = saveCategory("Developer Tools", "developer-tools");
        CategoryEntity deleted = saveCategory("Developer Legacy", "developer-legacy");
        deleted.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        categoryRepository.saveAndFlush(deleted);

        CategoryFilterRequest filter = CategoryFilterRequest.builder()
                .name(" developer ")
                .build();

        List<CategoryEntity> result = categoryRepository.findAll(CategorySpecification.fromFilter(filter));

        assertThat(result).extracting(CategoryEntity::getId).containsExactly(active.getId());
    }

    @Test
    void filtersByLowercaseSlug() {
        CategoryEntity target = saveCategory("Developer Tools", "developer-tools");
        saveCategory("Payments", "payments");

        CategoryFilterRequest filter = CategoryFilterRequest.builder()
                .slug(" DEVELOPER-TOOLS ")
                .build();

        List<CategoryEntity> result = categoryRepository.findAll(CategorySpecification.fromFilter(filter));

        assertThat(result).extracting(CategoryEntity::getId).containsExactly(target.getId());
    }

    @Test
    void alwaysAppliesDeletedAtIsNull() {
        CategoryEntity active = saveCategory("Analytics", "analytics");
        CategoryEntity deleted = saveCategory("Analytics Legacy", "analytics-legacy");
        deleted.setDeletedAt(OffsetDateTime.now(ZoneOffset.UTC));
        categoryRepository.saveAndFlush(deleted);

        List<CategoryEntity> result = categoryRepository.findAll(CategorySpecification.fromFilter(null));

        assertThat(result).extracting(CategoryEntity::getId).containsExactly(active.getId());
    }

    private CategoryEntity saveCategory(String name, String slug) {
        CategoryEntity entity = new CategoryEntity();
        entity.setName(name);
        entity.setSlug(slug);
        return categoryRepository.saveAndFlush(entity);
    }
}
