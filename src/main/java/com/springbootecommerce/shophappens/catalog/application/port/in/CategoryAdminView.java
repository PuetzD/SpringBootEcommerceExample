package com.springbootecommerce.shophappens.catalog.application.port.in;

public record CategoryAdminView(
        CategoryReference category,
        String name,
        String slug,
        CategoryRevision revision,
        long productCount) {}
