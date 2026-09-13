package com.springbootecommerce.shophappens.ordering.application.port.out;

import com.springbootecommerce.shophappens.ordering.application.port.in.CheckoutItem;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductVariantId;
import java.util.Optional;

public interface CheckoutCatalogGateway {
    Optional<CheckoutItem> find(ProductVariantId variant, int quantity);
}
