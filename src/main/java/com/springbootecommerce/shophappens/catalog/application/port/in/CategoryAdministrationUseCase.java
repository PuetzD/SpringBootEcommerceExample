package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.catalog.application.command.CreateCategoryCommand;
import com.springbootecommerce.shophappens.catalog.application.command.DeleteCategoryCommand;
import com.springbootecommerce.shophappens.catalog.application.command.UpdateCategoryCommand;

public interface CategoryAdministrationUseCase {
    long createCategory(CreateCategoryCommand command);

    long updateCategory(long categoryId, UpdateCategoryCommand command);

    boolean deleteCategory(DeleteCategoryCommand command);
}
