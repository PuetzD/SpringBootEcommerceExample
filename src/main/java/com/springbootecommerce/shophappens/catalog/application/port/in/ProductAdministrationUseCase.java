package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.catalog.application.command.CreateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.command.DeleteProductCommand;

public interface ProductAdministrationUseCase {
    long createProduct(CreateProductCommand command);

    boolean deleteProduct(DeleteProductCommand command);
}
