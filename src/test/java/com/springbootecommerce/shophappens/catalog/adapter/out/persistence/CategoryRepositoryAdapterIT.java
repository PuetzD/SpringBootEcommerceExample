package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import com.springbootecommerce.shophappens.catalog.application.port.out.CategoryRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.Category;
import com.springbootecommerce.shophappens.integration.AbstractIntegrationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

class CategoryRepositoryAdapterIT extends AbstractIntegrationTest {
    @Autowired CategoryRepository categories;
    @Autowired JdbcTemplate jdbc;

    @Test
    void ordersCategoriesByNameThenId() {
        insertCategory("Zebras");
        insertCategory("Apples");
        insertCategory("Mangoes");

        List<Category> results = categories.findAll();

        assertThat(results)
                .extracting(Category::name)
                .containsExactly("Apples", "Mangoes", "Zebras");
    }

    private void insertCategory(String name) {
        jdbc.update("insert into category (name, slug) values (?, ?)", name, name.toLowerCase());
    }
}
