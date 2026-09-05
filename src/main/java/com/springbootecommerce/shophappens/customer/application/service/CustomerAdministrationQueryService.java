package com.springbootecommerce.shophappens.customer.application.service;

import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminDetail;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminPage;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdminSearch;
import com.springbootecommerce.shophappens.customer.application.port.in.CustomerAdministrationQuery;
import com.springbootecommerce.shophappens.customer.application.port.out.CustomerRepository;
import com.springbootecommerce.shophappens.sharedkernel.identity.CustomerId;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CustomerAdministrationQueryService implements CustomerAdministrationQuery {
    private final CustomerRepository customerRepository;

    @Override
    public CustomerAdminPage searchCustomers(CustomerAdminSearch search) {
        return customerRepository.searchForAdministration(search);
    }

    @Override
    public Optional<CustomerAdminDetail> findCustomer(CustomerId customerId) {
        return customerRepository.findForAdministration(customerId);
    }
}
