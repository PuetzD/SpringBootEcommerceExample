package com.springbootecommerce.shophappens.catalog.application.port.out;

import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminPage;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryOption;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryRevision;
import com.springbootecommerce.shophappens.catalog.domain.model.Category;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import java.util.List;
import java.util.Optional;

public interface CategoryRepository {
    List<Category> findAll();

    Optional<Category> findBySlug(String slug);

    CategoryAdminPage searchForAdministration(CategoryAdminSearch search);

    List<CategoryOption> findOptionsForAdministration();

    Optional<VersionedCategory> findForAdministration(CategoryId id);

    VersionedCategory insertForAdministration(Category category);

    VersionedCategory updateForAdministration(Category category, CategoryRevision expectedRevision);

    long countProductsForAdministration(CategoryId id);

    boolean isReferencedByAnyProduct(CategoryId id);

    void deleteForAdministration(CategoryId id, CategoryRevision expectedRevision);
}
