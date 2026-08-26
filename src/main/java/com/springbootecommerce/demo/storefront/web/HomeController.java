package com.springbootecommerce.demo.storefront.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    @GetMapping("/")
    public String getHomepage() {
        return "homepage";
    }
}
