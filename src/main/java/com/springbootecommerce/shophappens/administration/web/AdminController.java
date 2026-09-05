package com.springbootecommerce.shophappens.administration.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin")
public class AdminController {
    @GetMapping({"", "/"})
    public String getBackend() {
        return "forward:/admin/index.html";
    }

    @GetMapping("/products")
    public String getProductsOverview() {
        return "forward:/admin/index.html";
    }

    @GetMapping("/categories")
    public String getCategoriesOverview() {
        return "forward:/admin/index.html";
    }

    @GetMapping("/orders")
    public String getOrdersOverview() {
        return "forward:/admin/index.html";
    }

    @GetMapping("/customers")
    public String getCustomersOverview() {
        return "forward:/admin/index.html";
    }

    @GetMapping("/storefront")
    public String getStorefrontOverview() {
        return "forward:/admin/index.html";
    }
}
