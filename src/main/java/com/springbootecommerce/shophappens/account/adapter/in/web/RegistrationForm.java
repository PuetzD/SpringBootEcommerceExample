package com.springbootecommerce.shophappens.account.adapter.in.web;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegistrationForm {

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email address")
    private String email;

    @Size(min = 12, max = 72, message = "Password must be between 12 and 72 characters")
    @NotBlank(message = "Password is required")
    private String password;
}
