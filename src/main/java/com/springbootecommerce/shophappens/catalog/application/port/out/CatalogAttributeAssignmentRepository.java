package com.springbootecommerce.shophappens.catalog.application.port.out;

import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeAssignment;

public interface CatalogAttributeAssignmentRepository {
    void save(CatalogAttributeAssignment assignment);
}
