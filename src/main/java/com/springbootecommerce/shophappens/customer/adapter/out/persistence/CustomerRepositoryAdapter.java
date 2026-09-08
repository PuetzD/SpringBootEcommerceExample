package com.springbootecommerce.shophappens.customer.adapter.out.persistence;

import com.springbootecommerce.shophappens.customer.application.port.in.AddressReference;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminAddressView;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminDetail;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminPage;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminSearch;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminSummary;
import com.springbootecommerce.shophappens.customer.application.port.out.CustomerRepository;
import com.springbootecommerce.shophappens.customer.domain.model.Address;
import com.springbootecommerce.shophappens.customer.domain.model.Customer;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
class CustomerRepositoryAdapter implements CustomerRepository {
    private final SpringDataCustomerRepository springData;
    private final CustomerPersistenceMapper mapper;

    @Override
    public Optional<Customer> findById(CustomerId id) {
        return springData.findDetailedById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(propagation = Propagation.MANDATORY)
    public Optional<Customer> findForUpdate(CustomerId id) {
        return springData.findRootForUpdate(id.value()).map(mapper::toDomain);
    }

    @Override
    public Optional<Customer> findByAccountId(AccountId id) {
        return springData.findByAccountId(id.value()).map(mapper::toDomain);
    }

    @Override
    public CustomerAdminPage searchForAdministration(CustomerAdminSearch search) {
        var page =
                springData.searchForAdministration(
                        escapeLikePattern(search.query()),
                        '\\',
                        PageRequest.of(
                                search.page(), search.size(), Sort.by(Sort.Direction.DESC, "id")));
        return new CustomerAdminPage(
                page.getContent().stream()
                        .map(
                                customer ->
                                        new CustomerAdminSummary(
                                                new CustomerId(customer.getId()),
                                                customer.getGivenName(),
                                                customer.getFamilyName(),
                                                customer.getContactEmail()))
                        .toList(),
                search.page(),
                search.size(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Override
    public Optional<CustomerAdminDetail> findForAdministration(CustomerId customerId) {
        return springData.findDetailedById(customerId.value()).map(this::toAdminDetail);
    }

    @Override
    @Transactional
    public Customer save(Customer customer) {
        if (customer.id().isEmpty()) {
            return mapper.toDomain(springData.saveAndFlush(mapper.toJpa(customer)));
        }

        var managed =
                springData.findDetailedById(customer.id().orElseThrow().value()).orElseThrow();
        boolean shippingOwnerChanged =
                !Objects.equals(defaultAddressId(managed, true), defaultAddressId(customer, true));
        boolean billingOwnerChanged =
                !Objects.equals(
                        defaultAddressId(managed, false), defaultAddressId(customer, false));
        if (shippingOwnerChanged || billingOwnerChanged) {
            managed.getAddresses()
                    .forEach(
                            address -> {
                                if (shippingOwnerChanged) address.setDefaultShipping(false);
                                if (billingOwnerChanged) address.setDefaultBilling(false);
                            });
            springData.flush();
        }
        mapper.applyToJpa(managed, customer);
        springData.flush();
        return mapper.toDomain(managed);
    }

    private Long defaultAddressId(CustomerJpaEntity customer, boolean shipping) {
        for (AddressJpaEntity address : customer.getAddresses()) {
            if (shipping ? address.isDefaultShipping() : address.isDefaultBilling()) {
                return address.getId();
            }
        }
        return null;
    }

    private Long defaultAddressId(Customer customer, boolean shipping) {
        for (Address address : customer.addresses()) {
            if (shipping ? address.defaultShipping() : address.defaultBilling()) {
                return address.id().map(id -> id.value()).orElse(null);
            }
        }
        return null;
    }

    private CustomerAdminDetail toAdminDetail(CustomerJpaEntity customer) {
        return new CustomerAdminDetail(
                new CustomerId(customer.getId()),
                new AccountId(customer.getAccountId()),
                customer.getGivenName(),
                customer.getFamilyName(),
                customer.getContactEmail(),
                customer.getAddresses().stream().map(this::toAdminAddress).toList());
    }

    private String escapeLikePattern(String query) {
        if (query == null) return null;
        return query.replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
    }

    private CustomerAdminAddressView toAdminAddress(AddressJpaEntity address) {
        return new CustomerAdminAddressView(
                new AddressReference(address.getId()),
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
