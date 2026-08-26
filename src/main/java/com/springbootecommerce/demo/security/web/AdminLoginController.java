package com.springbootecommerce.demo.security.web;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@RequiredArgsConstructor
@Controller
public class AdminLoginController {
  @GetMapping("/admin/login")
  public String login() {
    return "admin-login";
  }
}
