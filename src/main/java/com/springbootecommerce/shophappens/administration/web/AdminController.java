package com.springbootecommerce.shophappens.administration.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @GetMapping({
        "",
        "/",
        "/products",
        "/products/create",
        "/products/{id:[1-9][0-9]*}",
        "/categories",
        "/categories/create",
        "/categories/{id:[1-9][0-9]*}",
        "/orders",
        "/orders/{orderNumber:[A-Za-z0-9-]+}/show",
        "/customers",
        "/customers/{id:[1-9][0-9]*}/show",
        "/storefront"
    })
    public String adminApplication() {
        return "forward:/admin/index.html";
    }
}
