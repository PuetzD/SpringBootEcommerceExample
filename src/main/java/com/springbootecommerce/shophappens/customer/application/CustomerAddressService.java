package com.springbootecommerce.shophappens.customer.application;

import com.springbootecommerce.shophappens.customer.domain.Address;
import com.springbootecommerce.shophappens.customer.persistence.AddressRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CustomerAddressService {

    private final AddressRepository repository;

    public CustomerAddressService(AddressRepository repository) {
        this.repository = repository;
    }

    public List<AddressSnapshot> findForCustomer(Long customerId) {
        return repository.findByCustomerIdOrderByDefaultShippingDesc(customerId).stream()
                .map(this::snapshot)
                .toList();
    }

    public AddressSnapshot getOwned(Long customerId, Long addressId) {
        return repository
                .findByIdAndCustomerId(addressId, customerId)
                .map(this::snapshot)
                .orElseThrow(() -> new AddressNotOwnedException(customerId, addressId));
    }

    private AddressSnapshot snapshot(Address address) {
        return new AddressSnapshot(
                address.getId(),
                address.getRecipientName(),
                address.getCompanyName(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getRegion(),
                address.getPostalCode(),
                address.getCountryCode(),
                address.getPhoneNumber(),
                address.isDefaultShipping(),
                address.isDefaultBilling());
    }
}
