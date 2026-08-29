package com.springbootecommerce.shophappens.catalog.application.port.out;

import com.springbootecommerce.shophappens.catalog.domain.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    List<Category> findAll();

    Optional<Category> findBySlug(String slug);
}
