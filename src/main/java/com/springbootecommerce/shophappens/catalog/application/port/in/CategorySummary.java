package com.springbootecommerce.shophappens.catalog.application.port.in;

import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;

public record CategorySummary(CategoryId id, String name, String slug, long productCount) {}
