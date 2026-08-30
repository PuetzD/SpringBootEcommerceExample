package com.springbootecommerce.shophappens.administration.application.port.in;

public record CategoryAdminView(Long id, String name, String slug, long productCount) {}
