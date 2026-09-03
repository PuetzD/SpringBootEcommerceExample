package com.springbootecommerce.shophappens.catalog.application.port.in;

public record CategorySummary(long id, String name, String slug, long productCount) {}
