package com.springbootecommerce.shophappens.catalog.application.service;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminPage;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdministrationQuery;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
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
        return productRepository
                .searchForAdministration(new ProductAdminSearch(0, 100))
                .content()
                .stream()
                .filter(view -> view.product().equals(product))
                .findFirst();
    }
}
