package com.springbootecommerce.shophappens.customer.application.port.in;

public interface ManageCustomerAddressesUseCase {
    AddressReference save(CustomerReference customer, SaveAddressCommand command);

    void makeDefaultShipping(CustomerReference customer, AddressReference address);

    void makeDefaultBilling(CustomerReference customer, AddressReference address);

    void remove(CustomerReference customer, AddressReference address);

    record SaveAddressCommand(
            AddressReference address,
            String recipientName,
            String companyName,
            String addressLine1,
            String addressLine2,
            String city,
            String region,
            String postalCode,
            String countryCode,
            String phoneNumber,
            boolean defaultShipping,
            boolean defaultBilling) {}
}
