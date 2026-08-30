package com.springbootecommerce.shophappens.administration.web.api;

import com.springbootecommerce.shophappens.administration.application.port.in.ProductAdminQuery;
import com.springbootecommerce.shophappens.administration.application.port.in.ProductAdminView;
import com.springbootecommerce.shophappens.administration.application.port.in.ProductCategorySummary;
import com.springbootecommerce.shophappens.catalog.application.command.CreateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.command.DeleteProductCommand;
import com.springbootecommerce.shophappens.catalog.application.command.UpdateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdministrationUseCase;
import com.springbootecommerce.shophappens.sharedkernel.money.Money;
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
public class ProductAdminApiController {
    private final ProductAdminQuery productAdminQuery;
    private final ProductAdministrationUseCase productAdministrationUseCase;

    @GetMapping("/products")
    public PageResponse<ProductResponse> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        if (page < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Page must be >= 0");
        }
        if (size <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Size must be > 0");
        }
        List<ProductAdminView> results = productAdminQuery.findAll();
        int fromIndex = Math.min(page * size, results.size());
        int toIndex = Math.min(fromIndex + size, results.size());
        var slice = results.subList(fromIndex, toIndex);
        var content = slice.stream().map(this::toResponse).toList();
        var pageData = new PageImpl<>(content, PageRequest.of(page, size), results.size());
        return PageResponse.from(pageData);
    }

    @GetMapping("/products/{id}")
    public ProductResponse getProduct(@PathVariable long id) {
        return productAdminQuery
                .findById(id)
                .map(this::toResponse)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Product not found"));
    }

    @PostMapping("/products")
    public ResponseEntity<ProductResponse> createProduct(
            @Valid @RequestBody CreateProductRequest request) {
        long createdId =
                productAdministrationUseCase.createProduct(
                        new CreateProductCommand(
                                request.sku(),
                                request.name(),
                                request.description(),
                                new Money(request.price()),
                                request.stockQuantity(),
                                request.imageUrl()));
        ProductResponse body =
                productAdminQuery
                        .findById(createdId)
                        .map(this::toResponse)
                        .orElseThrow(
                                () ->
                                        new ResponseStatusException(
                                                HttpStatus.CREATED,
                                                "Product created but not retrievable"));
        return ResponseEntity.created(URI.create("/api/admin/products/" + createdId)).body(body);
    }

    @PutMapping("/products/{id}")
    public ProductResponse updateProduct(
            @PathVariable long id, @Valid @RequestBody UpdateProductRequest request) {
        productAdministrationUseCase.updateProduct(
                id,
                new UpdateProductCommand(
                        request.sku(),
                        request.name(),
                        request.description(),
                        new Money(request.price()),
                        request.stockQuantity(),
                        request.imageUrl(),
                        request.active()));
        return productAdminQuery
                .findById(id)
                .map(this::toResponse)
                .orElseThrow(
                        () ->
                                new ResponseStatusException(
                                        HttpStatus.NOT_FOUND, "Product not found"));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable long id) {
        boolean deleted = productAdministrationUseCase.deleteProduct(new DeleteProductCommand(id));
        if (!deleted) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Product not found");
        }
        return ResponseEntity.noContent().build();
    }

    private ProductResponse toResponse(ProductAdminView product) {
        return new ProductResponse(
                product.id(),
                product.sku(),
                product.name(),
                product.description(),
                product.price(),
                product.stockQuantity(),
                product.imageUrl(),
                product.active(),
                toCategorySummaries(product.categories()),
                "/api/admin/products/" + product.id(),
                "/api/admin/products/" + product.id(),
                "/api/admin/products/" + product.id());
    }

    private List<CategorySummaryResponse> toCategorySummaries(
            List<ProductCategorySummary> categories) {
        if (categories == null || categories.isEmpty()) {
            return List.of();
        }
        return categories.stream()
                .map(
                        category ->
                                new CategorySummaryResponse(
                                        category.id(), category.name(), category.slug()))
                .toList();
    }
}
