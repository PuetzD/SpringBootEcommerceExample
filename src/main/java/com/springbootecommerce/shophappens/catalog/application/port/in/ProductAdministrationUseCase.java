package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.catalog.application.command.CreateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.command.DeleteProductCommand;
import com.springbootecommerce.shophappens.catalog.application.command.UpdateProductCommand;

public interface ProductAdministrationUseCase {
    long createProduct(CreateProductCommand command);

    long updateProduct(long productId, UpdateProductCommand command);

    boolean deleteProduct(DeleteProductCommand command);
}
