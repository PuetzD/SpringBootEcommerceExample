package com.springbootecommerce.shophappens.ordering.application.port.out;

import java.util.List;

public interface CatalogPurchaseGateway {
    List<PurchasedProduct> purchase(List<RequestedProduct> products);
}
