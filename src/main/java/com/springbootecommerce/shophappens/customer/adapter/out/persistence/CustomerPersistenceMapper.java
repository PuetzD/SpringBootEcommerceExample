package com.springbootecommerce.shophappens.customer.adapter.out.persistence;

import com.springbootecommerce.shophappens.customer.domain.model.Address;
import com.springbootecommerce.shophappens.customer.domain.model.AddressDetails;
import com.springbootecommerce.shophappens.customer.domain.model.AddressId;
import com.springbootecommerce.shophappens.customer.domain.model.ContactEmail;
import com.springbootecommerce.shophappens.customer.domain.model.Customer;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
class CustomerPersistenceMapper {
    CustomerJpaEntity toJpa(Customer customer) {
        var jpa =
                CustomerJpaEntity.create(
                        customer.accountId().value(),
                        customer.givenName(),
                        customer.familyName(),
                        customer.contactEmail().value());
        customer.id().ifPresent(id -> jpa.setId(id.value()));
        for (Address address : customer.addresses()) {
            jpa.addAddress(toJpa(address));
        }
        return jpa;
    }

    Customer toDomain(CustomerJpaEntity jpa) {
        List<Address> addresses = jpa.getAddresses().stream().map(this::toDomain).toList();
        return Customer.restore(
                new CustomerId(jpa.getId()),
                new AccountId(jpa.getAccountId()),
                jpa.getGivenName(),
                jpa.getFamilyName(),
                new ContactEmail(jpa.getContactEmail()),
                addresses);
    }

    void applyToJpa(CustomerJpaEntity entity, Customer customer) {
        entity.setGivenName(customer.givenName());
        entity.setFamilyName(customer.familyName());
        entity.setContactEmail(customer.contactEmail().value());

        var existingById = new HashMap<Long, AddressJpaEntity>();
        for (AddressJpaEntity address : entity.getAddresses()) {
            existingById.put(address.getId(), address);
        }

        var retainedIds = new HashSet<Long>();
        for (Address address : customer.addresses()) {
            address.id()
                    .ifPresent(
                            id -> {
                                if (!existingById.containsKey(id.value())) {
                                    throw new IllegalArgumentException(
                                            "Address "
                                                    + id.value()
                                                    + " is not owned by customer "
                                                    + entity.getId());
                                }
                                retainedIds.add(id.value());
                            });
        }

        entity.getAddresses().removeIf(address -> !retainedIds.contains(address.getId()));
        for (Address address : customer.addresses()) {
            var addressId = address.id();
            if (addressId.isPresent()) {
                applyAddress(existingById.get(addressId.orElseThrow().value()), address);
            } else {
                entity.addAddress(toJpa(address));
            }
        }
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

    private void applyAddress(AddressJpaEntity entity, Address address) {
        var details = address.details();
        entity.setRecipientName(details.recipientName());
        entity.setCompanyName(details.companyName());
        entity.setAddressLine1(details.addressLine1());
        entity.setAddressLine2(details.addressLine2());
        entity.setCity(details.city());
        entity.setRegion(details.region());
        entity.setPostalCode(details.postalCode());
        entity.setCountryCode(details.countryCode());
        entity.setPhoneNumber(details.phoneNumber());
        entity.setDefaultShipping(address.defaultShipping());
        entity.setDefaultBilling(address.defaultBilling());
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
