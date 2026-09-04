package com.springbootecommerce.shophappens.administration.web.api;

import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.CreateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdministrationQuery;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdministrationUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductCategorySummary;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.UpdateProductCommand;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
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
public class ProductAdminApiController {
    private static final int MAX_PAGE_SIZE = 100;
    private final ProductAdministrationQuery productAdminQuery;
    private final ProductAdministrationUseCase productAdministrationUseCase;

    @GetMapping("/products")
    public PageResponse<ProductResponse> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Boolean active) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be >= 0");
        }
        if (size <= 0 || size > MAX_PAGE_SIZE) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Size must be between 1 and " + MAX_PAGE_SIZE);
        }
        var result =
                productAdminQuery.searchProducts(new ProductAdminSearch(page, size, q, active));
        return new PageResponse<>(
                result.content().stream().map(this::toResponse).toList(),
                result.page(),
                result.size(),
                result.totalElements(),
                result.totalPages());
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable long id) {
        return productAdminQuery
                .findProduct(new ProductReference(id))
                .map(this::toResponse)
                .orElseThrow(() -> new ProductNotFoundException(new ProductReference(id)));
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        ProductAdminView created =
                productAdministrationUseCase.createProduct(
                        new CreateProductCommand(
                                request.sku(),
                                request.name(),
                                request.description(),
                                new Money(request.price()),
                                request.stockQuantity(),
                                request.imageUrl(),
                                references(request.categoryIds())));
        return ResponseEntity.created(
                        URI.create("/api/admin/products/" + created.product().value()))
                .body(toResponse(created));
    }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(
            @PathVariable long id, @Valid @RequestBody UpdateProductRequest request) {
        ProductAdminView updated =
                productAdministrationUseCase.updateProduct(
                        new ProductReference(id),
                        new ProductRevision(request.revision()),
                        new UpdateProductCommand(
                                request.name(),
                                request.description(),
                                new Money(request.price()),
                                request.stockQuantity(),
                                request.imageUrl(),
                                request.active(),
                                references(request.categoryIds())));
        return toResponse(updated);
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(
            @PathVariable long id, @RequestHeader("If-Match") String ifMatch) {
        productAdministrationUseCase.deactivateProduct(
                new ProductReference(id),
                new ProductRevision(ExpectedRevisionParser.parse(ifMatch)));
        return ResponseEntity.noContent().build();
    }

    private Set<CategoryReference> references(Set<Long> ids) {
        return ids == null
                ? Set.of()
                : ids.stream().map(CategoryReference::new).collect(Collectors.toSet());
    }

    private ProductResponse toResponse(ProductAdminView product) {
        return new ProductResponse(
                product.product().value(),
                product.sku(),
                product.name(),
                product.description(),
                product.price().amount(),
                product.stockQuantity(),
                product.imageUrl(),
                product.active(),
                toCategorySummaries(product.categories()),
                product.revision().value(),
                "/api/admin/products/" + product.product().value(),
                "/api/admin/products/" + product.product().value(),
                "/api/admin/products/" + product.product().value());
    }

    private List<CategorySummaryResponse> toCategorySummaries(
            List<ProductCategorySummary> categories) {
        return categories.stream()
                .map(
                        category ->
                                new CategorySummaryResponse(
                                        category.category().value(),
                                        category.name(),
                                        category.slug()))
                .toList();
    }
}
