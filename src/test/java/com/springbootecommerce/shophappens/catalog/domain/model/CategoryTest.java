package com.springbootecommerce.shophappens.catalog.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CategoryTest {
    @Test
    void createsAndRenamesWithCanonicalNameAndSlug() {
        Category category = Category.create("  Home Office  ");

        category.rename(" Desk Tools ");

        assertThat(category.name()).isEqualTo("Desk Tools");
        assertThat(category.slug()).isEqualTo("desk-tools");
    }

    @Test
    void rejectsBlankNames() {
        assertThatThrownBy(() -> Category.create("  "))
                .isInstanceOf(IllegalArgumentException.class);
        Category category = Category.create("Home");
        assertThatThrownBy(() -> category.rename("  "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void restoresPersistedSlugWithoutRecomputingIt() {
        Category category = Category.restore(new CategoryId(1L), "Home Office", "legacy-home");

        assertThat(category.name()).isEqualTo("Home Office");
        assertThat(category.slug()).isEqualTo("legacy-home");
    }
}
