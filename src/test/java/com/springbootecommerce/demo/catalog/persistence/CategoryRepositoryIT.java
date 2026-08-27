package com.springbootecommerce.demo.catalog.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.demo.catalog.domain.Category;
import com.springbootecommerce.demo.integration.PostgresIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class CategoryRepositoryIT extends PostgresIntegrationTest {

    @Autowired CategoryRepository categoryRepository;

    @Test
    void findsCategoryBySlug() {
        var category = new Category();
        category.setName("Test Category");
        category.setSlug("test-category");
        categoryRepository.saveAndFlush(category);

        var found = categoryRepository.findBySlug("test-category");
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Test Category");
    }

    @Test
    void returnsEmptyForMissingSlug() {
        assertThat(categoryRepository.findBySlug("nonexistent")).isEmpty();
    }
}
