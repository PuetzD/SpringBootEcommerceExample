package com.springbootecommerce.shophappens.catalog.application.service;

import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminPage;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdministrationQuery;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryRevision;
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
public class CategoryAdministrationQueryService implements CategoryAdministrationQuery {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public CategoryAdminPage listCategories(CategoryAdminSearch search) {
        List<CategoryAdminView> categories =
                categoryRepository.findAll().stream().map(this::toView).toList();
        int from = Math.min(search.page() * search.size(), categories.size());
        int to = Math.min(from + search.size(), categories.size());
        int totalPages =
                categories.isEmpty()
                        ? 0
                        : (int) Math.ceil((double) categories.size() / search.size());
        return new CategoryAdminPage(
                categories.subList(from, to),
                search.page(),
                search.size(),
                categories.size(),
                totalPages);
    }

    @Override
    public Optional<CategoryAdminView> findCategory(CategoryReference category) {
        return categoryRepository.findAll().stream()
                .filter(
                        candidate ->
                                candidate
                                        .id()
                                        .map(id -> id.value() == category.value())
                                        .orElse(false))
                .findFirst()
                .map(this::toView);
    }

    @Override
    public List<com.springbootecommerce.shophappens.catalog.application.port.in.CategoryOption>
            listCategoryOptions() {
        return categoryRepository.findAll().stream()
                .map(
                        category ->
                                new com.springbootecommerce.shophappens.catalog.application.port.in
                                        .CategoryOption(
                                        new CategoryReference(category.id().orElseThrow().value()),
                                        category.name(),
                                        category.slug()))
                .toList();
    }

    private CategoryAdminView toView(Category category) {
        long id = category.id().orElseThrow().value();
        return new CategoryAdminView(
                new CategoryReference(id),
                category.name(),
                category.slug(),
                new CategoryRevision(0),
                productRepository.countActiveByCategoryId(new CategoryId(id)));
    }
}
