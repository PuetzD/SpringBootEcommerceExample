package com.springbootecommerce.shophappens.administration.web;

import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCatalogUseCase;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdministrationUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class AdminController {
    private final BrowseCatalogUseCase catalog;

    private final ProductAdministrationUseCase adminProduct;

    // private final  adminCategory;
    @GetMapping("/admin")
    public String getBackend(Model model) {
        return "admin/index";
    }
}
