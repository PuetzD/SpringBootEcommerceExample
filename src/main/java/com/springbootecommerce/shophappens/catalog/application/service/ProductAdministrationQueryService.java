package com.springbootecommerce.shophappens.catalog.application.service;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminPage;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdministrationQuery;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductAdministrationQueryService implements ProductAdministrationQuery {
    private final ProductRepository productRepository;

    @Override
    public ProductAdminPage searchProducts(ProductAdminSearch search) {
        return productRepository.searchForAdministration(search);
    }

    @Override
    public Optional<ProductAdminView> findProduct(ProductReference product) {
        if (product.value() < 1) {
            return Optional.empty();
        }
        return productRepository.findAdminViewById(new ProductId(product.value()));
    }
}
