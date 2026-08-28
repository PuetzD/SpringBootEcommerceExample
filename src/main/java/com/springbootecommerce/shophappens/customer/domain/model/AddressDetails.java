package com.springbootecommerce.shophappens.customer.domain.model;

public record AddressDetails(
        String recipientName,
        String companyName,
        String addressLine1,
        String addressLine2,
        String city,
        String region,
        String postalCode,
        String countryCode,
        String phoneNumber) {
    public AddressDetails {
        recipientName = required(recipientName, "Recipient name");
        addressLine1 = required(addressLine1, "Address line 1");
        city = required(city, "City");
        postalCode = required(postalCode, "Postal code");
        if (countryCode == null || !countryCode.matches("[A-Z]{2}")) {
            throw new IllegalArgumentException("Country code must be two uppercase letters");
        }
    }

    private static String required(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
        return value.strip();
    }
}
