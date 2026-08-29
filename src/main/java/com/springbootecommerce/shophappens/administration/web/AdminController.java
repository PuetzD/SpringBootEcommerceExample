package com.springbootecommerce.shophappens.administration.web;

import com.springbootecommerce.shophappens.catalog.application.port.in.BrowseCatalogUseCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class AdminController {
    private final BrowseCatalogUseCase catalog;
    //private final EditProductUseCase editProduct;

    @GetMapping("/admin/")
    public String getBackend(Model model) {
        return "admin/index";
    }
}
