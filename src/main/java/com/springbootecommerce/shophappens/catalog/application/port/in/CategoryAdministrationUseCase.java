package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.catalog.application.command.CreateCategoryCommand;
import com.springbootecommerce.shophappens.catalog.application.command.DeleteCategoryCommand;

public interface CategoryAdministrationUseCase {
    long createCategory(CreateCategoryCommand command);

    boolean deleteCategory(DeleteCategoryCommand command);
}
