package com.springbootecommerce.shophappens.catalog.application.port.in;

public record DeleteProductCommand(ProductReference product, ProductRevision expectedRevision) {}
