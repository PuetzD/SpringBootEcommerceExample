package com.springbootecommerce.shophappens.administration.application.service;

import com.springbootecommerce.shophappens.administration.application.port.in.CategoryAdminQuery;
import com.springbootecommerce.shophappens.administration.application.port.in.CategoryAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.out.CategoryRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.Category;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
class CategoryAdminQueryService implements CategoryAdminQuery {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public List<CategoryAdminView> findAll() {
        return categoryRepository.findAll().stream().map(this::toView).toList();
    }

    @Override
    public Optional<CategoryAdminView> findById(long categoryId) {
        return categoryRepository.findAll().stream()
                .filter(category -> category.id().map(id -> id.value() == categoryId).orElse(false))
                .findFirst()
                .map(this::toView);
    }

    private CategoryAdminView toView(Category category) {
        long id = category.id().orElseThrow().value();
        return new CategoryAdminView(
                id,
                category.name(),
                category.slug(),
                productRepository.countActiveByCategoryId(new CategoryId(id)));
    }
}
