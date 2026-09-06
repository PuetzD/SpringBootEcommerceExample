package com.springbootecommerce.shophappens.storefront.web;

import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCategoriesUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategorySummary;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class PublicNavigationModelAdvice {
    private final ObjectProvider<BrowseCategoriesUseCase> categories;

    @ModelAttribute("categories")
    public List<CategorySummary> categories() {
        var categoryQuery = categories.getIfAvailable();
        return categoryQuery == null ? List.of() : categoryQuery.findAllActive();
    }
}
