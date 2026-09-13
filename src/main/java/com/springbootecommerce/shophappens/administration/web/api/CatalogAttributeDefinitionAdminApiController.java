package com.springbootecommerce.shophappens.administration.web.api;

import com.springbootecommerce.shophappens.catalog.application.port.in.CatalogAttributeDefinitionAdministrationUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.CatalogAttributeDefinitionView;
import com.springbootecommerce.shophappens.catalog.application.port.in.CreateCatalogAttributeDefinitionCommand;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/attribute-definitions")
public class CatalogAttributeDefinitionAdminApiController {
    private final CatalogAttributeDefinitionAdministrationUseCase service;

    @GetMapping
    public List<CatalogAttributeDefinitionView> list() {
        return service.list();
    }

    @PostMapping
    public CatalogAttributeDefinitionView create(
            @Valid @RequestBody CreateCatalogAttributeDefinitionRequest request) {
        return service.create(
                new CreateCatalogAttributeDefinitionCommand(
                        request.code(),
                        request.label(),
                        request.type(),
                        request.scope(),
                        request.option()));
    }

    @PostMapping("/{code}/values")
    public CatalogAttributeDefinitionView addValue(
            @PathVariable @NotBlank String code,
            @Valid @RequestBody CreateCatalogAttributeValueRequest request) {
        return service.addAllowedValue(code, request.code(), request.label());
    }
}
