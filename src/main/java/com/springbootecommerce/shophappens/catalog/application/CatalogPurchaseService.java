package com.springbootecommerce.shophappens.catalog.application;

import com.springbootecommerce.shophappens.catalog.domain.ProductUnavailableException;
import com.springbootecommerce.shophappens.catalog.persistence.ProductRepository;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CatalogPurchaseService {

    private final ProductRepository productRepository;

    public CatalogPurchaseService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public List<PurchasedProduct> reserve(List<PurchaseRequest> requests) {
        return requests.stream()
                .sorted(Comparator.comparing(PurchaseRequest::productId))
                .map(this::reserveOne)
                .toList();
    }

    private PurchasedProduct reserveOne(PurchaseRequest request) {
        var product =
                productRepository
                        .findById(request.productId())
                        .orElseThrow(
                                () -> new ProductUnavailableException(request.productId(), null));
        product.reserveStock(request.quantity());
        return new PurchasedProduct(
                request.productId(),
                product.getSku(),
                product.getName(),
                product.getPrice(),
                request.quantity());
    }
}
