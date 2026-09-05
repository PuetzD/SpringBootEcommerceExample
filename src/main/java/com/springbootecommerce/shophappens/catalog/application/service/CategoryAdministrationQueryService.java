package com.springbootecommerce.shophappens.catalog.application.service;

import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminPage;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdministrationQuery;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryOption;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryRevision;
import com.springbootecommerce.shophappens.catalog.application.port.out.CategoryRepository;
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

    @Override
    public CategoryAdminPage listCategories(CategoryAdminSearch search) {
        return categoryRepository.searchForAdministration(search);
    }

    @Override
    public Optional<CategoryAdminView> findCategory(CategoryReference category) {
        CategoryId id = new CategoryId(category.value());
        return categoryRepository
                .findForAdministration(id)
                .map(
                        versioned -> {
                            var current = versioned.category();
                            return new CategoryAdminView(
                                    category,
                                    current.name(),
                                    current.slug(),
                                    new CategoryRevision(versioned.revision()),
                                    categoryRepository.countProductsForAdministration(id));
                        });
    }

    @Override
    public List<CategoryOption> listCategoryOptions() {
        return categoryRepository.findOptionsForAdministration();
    }
}
