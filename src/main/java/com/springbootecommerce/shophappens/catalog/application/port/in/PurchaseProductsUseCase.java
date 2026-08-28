package com.springbootecommerce.shophappens.catalog.application.port.in;

import java.util.List;

public interface PurchaseProductsUseCase {
    List<PurchasedProductSnapshot> purchase(List<PurchaseLine> lines);
}
