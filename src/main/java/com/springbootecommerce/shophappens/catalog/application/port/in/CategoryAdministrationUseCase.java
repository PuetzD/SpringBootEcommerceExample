package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.catalog.application.command.CreateCategoryCommand;
import com.springbootecommerce.shophappens.catalog.application.command.DeleteCategoryCommand;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;

public interface CategoryAdministrationUseCase {
    CategoryId createCategory(CreateCategoryCommand command);

    boolean deleteCategory(DeleteCategoryCommand command);
}
