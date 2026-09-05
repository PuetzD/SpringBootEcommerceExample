package com.springbootecommerce.shophappens.catalog.application.service;

import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdministrationUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryInUseException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.CreateCategoryCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.InvalidCatalogOperationException;
import com.springbootecommerce.shophappens.catalog.application.port.in.RenameCategoryCommand;
import com.springbootecommerce.shophappens.catalog.application.port.out.CategoryRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.VersionedCategory;
import com.springbootecommerce.shophappens.catalog.domain.model.Category;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryAdministrationService implements CategoryAdministrationUseCase {
    private final CategoryRepository categories;

    public CategoryAdministrationService(CategoryRepository categories) {
        this.categories = categories;
    }

    @Override
    @Transactional
    public CategoryAdminView createCategory(CreateCategoryCommand command) {
        try {
            VersionedCategory saved =
                    categories.insertForAdministration(Category.create(command.name()));
            return toView(saved);
        } catch (IllegalArgumentException exception) {
            throw new InvalidCatalogOperationException(exception.getMessage());
        }
    }

    @Override
    @Transactional
    public CategoryAdminView renameCategory(
            CategoryReference reference,
            CategoryRevision expectedRevision,
            RenameCategoryCommand command) {
        VersionedCategory current =
                categories
                        .findForAdministration(new CategoryId(reference.value()))
                        .orElseThrow(() -> new CategoryNotFoundException(reference));
        try {
            current.category().rename(command.name());
            return toView(categories.updateForAdministration(current.category(), expectedRevision));
        } catch (CategoryNotFoundException
                | com.springbootecommerce.shophappens.catalog.application.port.in
                        .DuplicateCategoryException
                | com.springbootecommerce.shophappens.catalog.application.port.in
                        .StaleCategoryRevisionException
                | CategoryInUseException exception) {
            throw exception;
        } catch (IllegalArgumentException exception) {
            throw new InvalidCatalogOperationException(exception.getMessage());
        }
    }

    @Override
    @Transactional
    public void deleteCategory(CategoryReference reference, CategoryRevision expectedRevision) {
        CategoryId id = new CategoryId(reference.value());
        if (categories.findForAdministration(id).isEmpty()) {
            throw new CategoryNotFoundException(reference);
        }
        if (categories.isReferencedByAnyProduct(id)) {
            throw new CategoryInUseException(reference);
        }
        categories.deleteForAdministration(id, expectedRevision);
    }

    private CategoryAdminView toView(VersionedCategory versioned) {
        Category category = versioned.category();
        long id = category.id().orElseThrow().value();
        return new CategoryAdminView(
                new CategoryReference(id),
                category.name(),
                category.slug(),
                new CategoryRevision(versioned.revision()),
                categories.countProductsForAdministration(new CategoryId(id)));
    }
}
