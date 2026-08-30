package com.springbootecommerce.shophappens.administration.application.port.in;

import java.util.List;
import java.util.Optional;

public interface ProductAdminQuery {
    List<ProductAdminView> findAll();

    Optional<ProductAdminView> findById(long productId);
}
