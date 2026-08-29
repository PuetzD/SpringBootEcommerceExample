package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.catalog.application.command.CreateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.command.DeleteProductCommand;
import com.springbootecommerce.shophappens.ordering.domain.model.ProductId;

public interface ProductAdministrationUseCase {
    ProductId createProduct(CreateProductCommand command);
    boolean deleteProduct(DeleteProductCommand command);
}
