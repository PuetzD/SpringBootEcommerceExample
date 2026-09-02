package com.springbootecommerce.shophappens.catalog.application.port.in;

public interface CategoryAdministrationUseCase {
    CategoryAdminView createCategory(CreateCategoryCommand command);

    CategoryAdminView renameCategory(
            CategoryReference category,
            CategoryRevision expectedRevision,
            RenameCategoryCommand command);

    void deleteCategory(CategoryReference category, CategoryRevision expectedRevision);
}
