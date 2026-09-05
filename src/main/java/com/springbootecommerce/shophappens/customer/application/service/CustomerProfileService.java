package com.springbootecommerce.shophappens.customer.application.service;

import com.springbootecommerce.shophappens.customer.application.CustomerNotFoundException;
import com.springbootecommerce.shophappens.customer.application.CustomerProfileAlreadyExistsException;
import com.springbootecommerce.shophappens.customer.application.port.in.AddressReference;
import com.springbootecommerce.shophappens.customer.application.port.in.AddressSnapshot;
import com.springbootecommerce.shophappens.customer.application.port.in.CreateCustomerProfileUseCase;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerReferenceQuery;
import com.springbootecommerce.shophappens.customer.application.port.in.ExternalAccountId;
import com.springbootecommerce.shophappens.customer.application.port.in.ManageCustomerAddressesUseCase;
import com.springbootecommerce.shophappens.customer.application.port.in.OwnedAddressQuery;
import com.springbootecommerce.shophappens.customer.application.port.in.OwnedAddressUnavailableException;
import com.springbootecommerce.shophappens.customer.application.port.out.CustomerRepository;
import com.springbootecommerce.shophappens.customer.domain.model.Address;
import com.springbootecommerce.shophappens.customer.domain.model.AddressDetails;
import com.springbootecommerce.shophappens.customer.domain.model.AddressId;
import com.springbootecommerce.shophappens.customer.domain.model.ContactEmail;
import com.springbootecommerce.shophappens.customer.domain.model.Customer;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomerProfileService
        implements CreateCustomerProfileUseCase,
                ManageCustomerAddressesUseCase,
                OwnedAddressQuery,
                CustomerReferenceQuery {

    private final CustomerRepository customers;

    @Override
    @Transactional
    public CustomerReference create(
            ExternalAccountId accountId, String givenName, String familyName, String contactEmail) {
        var internalAccountId = new AccountId(accountId.value());
        if (customers.findByAccountId(internalAccountId).isPresent()) {
            throw new CustomerProfileAlreadyExistsException(accountId);
        }
        var saved =
                customers.save(
                        Customer.create(
                                internalAccountId,
                                givenName,
                                familyName,
                                new ContactEmail(contactEmail)));
        return new CustomerReference(saved.id().orElseThrow().value());
    }

    @Override
    @Transactional
    public AddressReference save(CustomerReference customer, SaveAddressCommand command) {
        var aggregate = requireCustomer(customer);
        var details =
                new AddressDetails(
                        command.recipientName(),
                        command.companyName(),
                        command.addressLine1(),
                        command.addressLine2(),
                        command.city(),
                        command.region(),
                        command.postalCode(),
                        command.countryCode(),
                        command.phoneNumber());

        if (command.address() == null) {
            aggregate.addAddress(details, command.defaultShipping(), command.defaultBilling());
        } else {
            aggregate.updateAddress(
                    new AddressId(command.address().value()),
                    details,
                    command.defaultShipping(),
                    command.defaultBilling());
        }

        var saved = customers.save(aggregate);
        Address target =
                command.address() == null
                        ? saved.addresses().get(saved.addresses().size() - 1)
                        : saved.address(new AddressId(command.address().value()));
        return new AddressReference(target.id().orElseThrow().value());
    }

    @Override
    @Transactional
    public void remove(CustomerReference customer, AddressReference address) {
        var aggregate = requireCustomer(customer);
        aggregate.removeAddress(new AddressId(address.value()));
        customers.save(aggregate);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AddressSnapshot> findForCustomer(CustomerReference customer) {
        var aggregate = requireCustomer(customer);
        return aggregate.addresses().stream()
                .sorted(Comparator.comparing(Address::defaultShipping).reversed())
                .map(address -> snapshot(customer, address))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public AddressSnapshot getOwned(CustomerReference customer, AddressReference address) {
        var aggregate = requireCustomer(customer);
        try {
            return snapshot(customer, aggregate.address(new AddressId(address.value())));
        } catch (
                com.springbootecommerce.shophappens.customer.domain.exception
                                .AddressNotOwnedException
                        exception) {
            throw new OwnedAddressUnavailableException(exception.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CustomerReference> findByExternalAccountId(ExternalAccountId accountId) {
        return customers
                .findByAccountId(new AccountId(accountId.value()))
                .flatMap(customer -> customer.id().map(id -> new CustomerReference(id.value())));
    }

    private Customer requireCustomer(CustomerReference customer) {
        return customers
                .findById(new CustomerId(customer.value()))
                .orElseThrow(() -> new CustomerNotFoundException(customer));
    }

    private AddressSnapshot snapshot(CustomerReference customer, Address address) {
        return new AddressSnapshot(
                customer,
                new AddressReference(address.id().orElseThrow().value()),
                address.details().recipientName(),
                address.details().companyName(),
                address.details().addressLine1(),
                address.details().addressLine2(),
                address.details().city(),
                address.details().region(),
                address.details().postalCode(),
                address.details().countryCode(),
                address.details().phoneNumber(),
                address.defaultShipping(),
                address.defaultBilling());
    }
}
