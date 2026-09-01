package com.springbootecommerce.shophappens.catalog.application.port.in;

import java.util.List;
import java.util.Optional;

public interface CategoryAdministrationQuery {
    CategoryAdminPage listCategories(CategoryAdminSearch search);

    Optional<CategoryAdminView> findCategory(CategoryReference category);

    List<CategoryOption> listCategoryOptions();
}
