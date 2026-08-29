package com.springbootecommerce.shophappens.catalog.application.service;

import com.springbootecommerce.shophappens.catalog.application.command.CreateProductCommand;
import com.springbootecommerce.shophappens.catalog.application.command.DeleteProductCommand;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdministrationUseCase;
import org.springframework.stereotype.Service;

@Service
public class ProductAdministrationService implements ProductAdministrationUseCase {
    @Override
    public long createProduct(CreateProductCommand command) {
        return 0;
    }

    @Override
    public boolean deleteProduct(DeleteProductCommand command) {
        return false;
    }
}
