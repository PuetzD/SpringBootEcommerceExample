package com.springbootecommerce.shophappens.customer.application.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminAddressView;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminDetail;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminPage;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminSearch;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminSummary;
import com.springbootecommerce.shophappens.customer.application.port.out.CustomerRepository;
import com.springbootecommerce.shophappens.sharedkernel.identity.AccountId;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CustomerAdministrationQueryServiceTest {

    @Mock private CustomerRepository customerRepository;

    @Test
    void searchCustomersReturnsTheRepositoryPage() {
        var search = new CustomerAdminSearch(0, 20, "Ada");
        var summary =
                new CustomerAdminSummary(new CustomerId(7), "Ada", "Lovelace", "ada@example.com");
        var expected = new CustomerAdminPage(List.of(summary), 0, 20, 1, 1);
        when(customerRepository.searchForAdministration(search)).thenReturn(expected);

        var service = new CustomerAdministrationQueryService(customerRepository);

        assertEquals(expected, service.searchCustomers(search));
        verify(customerRepository).searchForAdministration(search);
    }

    @Test
    void findCustomerReturnsTheRepositoryDetail() {
        var customerId = new CustomerId(7);
        var address =
                new CustomerAdminAddressView(
                        new com.springbootecommerce.shophappens.customer.domain.model.AddressId(3),
                        "Ada Lovelace",
                        null,
                        "1 Main Street",
                        null,
                        "Greymoor",
                        null,
                        "35037",
                        "DE",
                        null,
                        true,
                        true);
        var detail =
                new CustomerAdminDetail(
                        customerId,
                        new AccountId(9),
                        "Ada",
                        "Lovelace",
                        "ada@example.com",
                        List.of(address));
        when(customerRepository.findForAdministration(customerId)).thenReturn(Optional.of(detail));

        var service = new CustomerAdministrationQueryService(customerRepository);

        assertEquals(Optional.of(detail), service.findCustomer(customerId));
        verify(customerRepository).findForAdministration(customerId);
    }

    @Test
    void detailDefensivelyCopiesAddresses() {
        var addresses = new java.util.ArrayList<CustomerAdminAddressView>();
        var detail =
                new CustomerAdminDetail(
                        new CustomerId(7),
                        new AccountId(9),
                        "Ada",
                        "Lovelace",
                        "ada@example.com",
                        addresses);

        addresses.add(
                new CustomerAdminAddressView(
                        new com.springbootecommerce.shophappens.customer.domain.model.AddressId(3),
                        "Ada Lovelace",
                        null,
                        "1 Main Street",
                        null,
                        "Greymoor",
                        null,
                        "35037",
                        "DE",
                        null,
                        true,
                        true));

        assertEquals(List.of(), detail.addresses());
    }
}
