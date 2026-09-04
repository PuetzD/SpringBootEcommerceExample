package com.springbootecommerce.shophappens.administration.web.api;

import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdministrationQuery;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdministrationUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryOption;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.CreateCategoryCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.RenameCategoryCommand;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
        return new PageResponse<>(
                content,
                results.page(),
                results.size(),
                results.totalElements(),
                results.totalPages());
    }

    @GetMapping("/categories/{id}")
    public CategoryResponse getCategory(@PathVariable long id) {
        return categoryAdminQuery
                .findCategory(new CategoryReference(id))
                .map(this::toResponse)
                .orElseThrow(() -> new CategoryNotFoundException(new CategoryReference(id)));
    }

    @GetMapping("/categories/options")
    public List<CategoryOption> listCategoryOptions() {
        return categoryAdminQuery.listCategoryOptions();
    }

    @PostMapping("/categories")
    public ResponseEntity<CategoryResponse> createCategory(
            @Valid @RequestBody CreateCategoryRequest request) {
        var created =
                categoryAdministrationUseCase.createCategory(
                        new CreateCategoryCommand(request.name()));
        long createdId = created.category().value();
        return ResponseEntity.created(URI.create("/api/admin/categories/" + createdId))
                .body(toResponse(created));
    }

    @PutMapping("/categories/{id}")
    public CategoryResponse updateCategory(
            @PathVariable long id,
            @RequestHeader("If-Match") String ifMatch,
            @Valid @RequestBody UpdateCategoryRequest request) {
        return toResponse(
                categoryAdministrationUseCase.renameCategory(
                        new CategoryReference(id),
                        new CategoryRevision(ExpectedRevisionParser.parse(ifMatch)),
                        new RenameCategoryCommand(request.name())));
    }

    @DeleteMapping("/categories/{id}")
    public ResponseEntity<Void> deleteCategory(
            @PathVariable long id, @RequestHeader("If-Match") String ifMatch) {
        categoryAdministrationUseCase.deleteCategory(
                new CategoryReference(id),
                new CategoryRevision(ExpectedRevisionParser.parse(ifMatch)));
        return ResponseEntity.noContent().build();
    }

    private CategoryResponse toResponse(CategoryAdminView category) {
        return new CategoryResponse(
                category.category().value(),
                category.name(),
                category.slug(),
                category.revision().value(),
                category.productCount(),
                "/api/admin/categories/" + category.category().value(),
                "/api/admin/categories/" + category.category().value(),
                "/api/admin/categories/" + category.category().value());
    }
}
