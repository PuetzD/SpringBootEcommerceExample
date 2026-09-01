package com.springbootecommerce.shophappens.administration.web.api;

import com.springbootecommerce.shophappens.catalog.application.command.CreateCategoryCommand;
import com.springbootecommerce.shophappens.catalog.application.command.DeleteCategoryCommand;
import com.springbootecommerce.shophappens.catalog.application.command.UpdateCategoryCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdministrationQuery;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdministrationUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import jakarta.validation.Valid;

import java.net.URI;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class CategoryAdminApiController {
    private final CategoryAdministrationQuery categoryAdminQuery;
    private final CategoryAdministrationUseCase categoryAdministrationUseCase;

    @GetMapping("/categories")
    public PageResponse<CategoryResponse> listCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be >= 0");
        }
        if (size <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Size must be > 0");
        }
        var results = categoryAdminQuery.listCategories(new CategoryAdminSearch(page, size));
        var content = results.content().stream().map(this::toResponse).toList();
        var pageData =
                new PageImpl<>(content, PageRequest.of(page, size), results.totalElements());
        return PageResponse.from(pageData);
    }

    @GetMapping("/categories/{id}")
    public CategoryResponse getCategory(@PathVariable long id) {
        return categoryAdminQuery
                .findCategory(new CategoryReference(id))
                .map(this::toResponse)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Category not found"));
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        long createdId =
                categoryAdministrationUseCase.createCategory(
                        new CreateCategoryCommand(request.name()));
        CategoryResponse body =
                categoryAdminQuery
                        .findCategory(new CategoryReference(createdId))
                        .map(this::toResponse)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.CREATED,
                                                "Category created but not retrievable"));
        return ResponseEntity.created(URI.create("/api/admin/categories/" + createdId)).body(body);
    }

    @PutMapping("/categories/{id}")
    public CategoryResponse updateCategory(
            @PathVariable long id, @Valid @RequestBody UpdateCategoryRequest request) {
        categoryAdministrationUseCase.updateCategory(id, new UpdateCategoryCommand(request.name()));
        return categoryAdminQuery
                .findCategory(new CategoryReference(id))
                .map(this::toResponse)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Category not found"));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable long id) {
        boolean deleted =
                categoryAdministrationUseCase.deleteCategory(new DeleteCategoryCommand(id));
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category not found");
        }
        return ResponseEntity.noContent().build();
    }

    private CategoryResponse toResponse(CategoryAdminView category) {
        return new CategoryResponse(
                category.category().value(),
                category.name(),
                category.slug(),
                category.productCount(),
                "/api/admin/categories/" + category.category().value(),
                "/api/admin/categories/" + category.category().value(),
                "/api/admin/categories/" + category.category().value());
    }
}
