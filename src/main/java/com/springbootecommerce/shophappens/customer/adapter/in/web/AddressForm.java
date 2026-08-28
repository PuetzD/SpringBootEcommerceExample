package com.springbootecommerce.shophappens.customer.adapter.in.web;

import com.springbootecommerce.shophappens.customer.application.port.in.AddressSnapshot;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

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

    public String getRecipientName() {
        return recipientName;
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getAddressLine1() {
        return addressLine1;
    }

    public void setAddressLine1(String addressLine1) {
        this.addressLine1 = addressLine1;
    }

    public String getAddressLine2() {
        return addressLine2;
    }

    public void setAddressLine2(String addressLine2) {
        this.addressLine2 = addressLine2;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public boolean isDefaultShipping() {
        return defaultShipping;
    }

    public void setDefaultShipping(boolean defaultShipping) {
        this.defaultShipping = defaultShipping;
    }

    public boolean isDefaultBilling() {
        return defaultBilling;
    }

    public void setDefaultBilling(boolean defaultBilling) {
        this.defaultBilling = defaultBilling;
    }
}
