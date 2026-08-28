package com.springbootecommerce.shophappens.customer.adapter.out.persistence;

import com.springbootecommerce.shophappens.customer.domain.model.AccountId;
import com.springbootecommerce.shophappens.customer.domain.model.Address;
import com.springbootecommerce.shophappens.customer.domain.model.AddressDetails;
import com.springbootecommerce.shophappens.customer.domain.model.AddressId;
import com.springbootecommerce.shophappens.customer.domain.model.Customer;
import com.springbootecommerce.shophappens.customer.domain.model.CustomerId;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class CustomerPersistenceMapper {
    CustomerJpaEntity toJpa(Customer customer) {
        var jpa = CustomerJpaEntity.create(customer.accountId().value());
        customer.id().ifPresent(id -> jpa.setId(id.value()));
        customer.addresses().forEach(address -> jpa.addAddress(toJpa(address)));
        return jpa;
    }

    Customer toDomain(CustomerJpaEntity jpa) {
        List<Address> addresses = jpa.getAddresses().stream().map(this::toDomain).toList();
        return Customer.restore(
                new CustomerId(jpa.getId()), new AccountId(jpa.getAccountId()), addresses);
    }

    private AddressJpaEntity toJpa(Address address) {
        var details = address.details();
        var jpa =
                AddressJpaEntity.create(
                        details.recipientName(),
                        details.companyName(),
                        details.addressLine1(),
                        details.addressLine2(),
                        details.city(),
                        details.region(),
                        details.postalCode(),
                        details.countryCode(),
                        details.phoneNumber(),
                        address.defaultShipping(),
                        address.defaultBilling());
        address.id().ifPresent(id -> jpa.setId(id.value()));
        return jpa;
    }

    private Address toDomain(AddressJpaEntity jpa) {
        return Address.restore(
                new AddressId(jpa.getId()),
                new AddressDetails(
                        jpa.getRecipientName(),
                        jpa.getCompanyName(),
                        jpa.getAddressLine1(),
                        jpa.getAddressLine2(),
                        jpa.getCity(),
                        jpa.getRegion(),
                        jpa.getPostalCode(),
                        jpa.getCountryCode(),
                        jpa.getPhoneNumber()),
                jpa.isDefaultShipping(),
                jpa.isDefaultBilling());
    }
}
