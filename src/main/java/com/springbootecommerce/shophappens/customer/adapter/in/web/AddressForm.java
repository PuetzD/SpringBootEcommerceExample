package com.springbootecommerce.shophappens.customer.adapter.in.web;

import com.springbootecommerce.shophappens.customer.application.port.in.AddressSnapshot;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AddressForm {

    @NotBlank(message = "Recipient name is required")
    @Size(max = 200)
    private String recipientName;

    @Size(max = 200)
    private String companyName;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 200)
    private String addressLine1;

    @Size(max = 200)
    private String addressLine2;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @Size(max = 100)
    private String region;

    @NotBlank(message = "Postal code is required")
    @Size(max = 32)
    private String postalCode;

    @NotBlank(message = "Country is required")
    @Pattern(regexp = "[A-Z]{2}", message = "Country must be a two-letter code (e.g. DE, US)")
    private String countryCode;

    @Size(max = 32)
    private String phoneNumber;

    private boolean defaultShipping;
    private boolean defaultBilling;

    public static AddressForm from(AddressSnapshot snapshot) {
        var form = new AddressForm();
        form.recipientName = snapshot.recipientName();
        form.companyName = snapshot.companyName();
        form.addressLine1 = snapshot.addressLine1();
        form.addressLine2 = snapshot.addressLine2();
        form.city = snapshot.city();
        form.region = snapshot.region();
        form.postalCode = snapshot.postalCode();
        form.countryCode = snapshot.countryCode();
        form.phoneNumber = snapshot.phoneNumber();
        form.defaultShipping = snapshot.defaultShipping();
        form.defaultBilling = snapshot.defaultBilling();
        return form;
    }
}
