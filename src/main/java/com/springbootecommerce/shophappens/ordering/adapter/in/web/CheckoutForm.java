package com.springbootecommerce.shophappens.ordering.adapter.in.web;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CheckoutForm {
    @NotNull(message = "Checkout reference is required")
    private UUID checkoutId;

    @NotNull(message = "Select a shipping address")
    @Positive(message = "Select a shipping address")
    private Long shippingAddressId;

    @NotNull(message = "Select a billing address")
    @Positive(message = "Select a billing address")
    private Long billingAddressId;
}
