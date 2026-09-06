package com.springbootecommerce.shophappens.administration.web.api;

import com.springbootecommerce.shophappens.catalog.application.port.in.AssignCatalogAttributeCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.CatalogAttributeAssignmentUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
public class CatalogAttributeAssignmentAdminApiController {
    private final CatalogAttributeAssignmentUseCase service;

    @PostMapping("/products/{productId}/attributes")
    public ResponseEntity<Void> assignToProduct(
            @PathVariable @Positive long productId,
            @Valid @RequestBody AssignCatalogAttributeRequest request) {
        service.assignToProduct(
                productId,
                new AssignCatalogAttributeCommand(request.definitionCode(), request.value()));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/products/{productId}/variants/{variantId}/attributes")
    public ResponseEntity<Void> assignToVariant(
            @PathVariable @Positive long variantId,
            @Valid @RequestBody AssignCatalogAttributeRequest request) {
        service.assignToVariant(
                variantId,
                new AssignCatalogAttributeCommand(request.definitionCode(), request.value()));
        return ResponseEntity.noContent().build();
    }
}
