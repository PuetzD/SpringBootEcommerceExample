package com.springbootecommerce.shophappens.administration.application.port.in;

import java.util.List;
import java.util.Optional;

public interface CategoryAdminQuery {
    List<CategoryAdminView> findAll();

    Optional<CategoryAdminView> findById(long categoryId);
}
