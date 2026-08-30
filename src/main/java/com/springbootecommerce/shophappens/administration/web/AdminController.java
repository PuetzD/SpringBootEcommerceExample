package com.springbootecommerce.shophappens.administration.web;

import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdministrationUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdministrationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequiredArgsConstructor
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final ProductAdministrationUseCase adminProduct;

    private final CategoryAdministrationUseCase adminCategory;

    @GetMapping("/")
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

    @GetMapping("/login")
    public String getLogin() {
        return "admin/login";
    }

    @GetMapping("/logout")
    public String getLogout() {
        return "admin/login";
    }

    @GetMapping("/{*path}")
    public String fallback() {
        return "forward:/admin/index.html";
    }
}
