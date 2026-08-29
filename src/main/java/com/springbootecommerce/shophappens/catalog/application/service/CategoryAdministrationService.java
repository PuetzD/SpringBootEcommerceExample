package com.springbootecommerce.shophappens.catalog.application.service;

import com.springbootecommerce.shophappens.catalog.application.command.CreateCategoryCommand;
import com.springbootecommerce.shophappens.catalog.application.command.DeleteCategoryCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdministrationUseCase;
import org.springframework.stereotype.Service;

@Service
public class CategoryAdministrationService implements CategoryAdministrationUseCase {
    @Override
    public long createCategory(CreateCategoryCommand command) {
        return 0;
    }

    @Override
    public boolean deleteCategory(DeleteCategoryCommand command) {
        return false;
    }
}
