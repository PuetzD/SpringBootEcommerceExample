package com.springbootecommerce.shophappens.administration.web.api;

public record CategoryResponse(
        Long id,
        String name,
        String slug,
        long productCount,
        String self,
        String edit,
        String delete) {}
