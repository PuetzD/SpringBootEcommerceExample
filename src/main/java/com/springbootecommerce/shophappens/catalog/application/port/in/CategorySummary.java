package com.springbootecommerce.shophappens.catalog.application.port.in;

public record CategorySummary(CategoryReference id, String name, String slug, long productCount) {}
