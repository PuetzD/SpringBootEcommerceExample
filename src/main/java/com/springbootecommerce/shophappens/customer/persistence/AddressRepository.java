package com.springbootecommerce.shophappens.customer.persistence;

import com.springbootecommerce.shophappens.customer.domain.Address;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
    List<Address> findByCustomerIdOrderByDefaultShippingDesc(Long customerId);

    List<Address> findByCustomerIdAndDefaultShippingTrue(Long customerId);

    List<Address> findByCustomerIdAndDefaultBillingTrue(Long customerId);

    Optional<Address> findByIdAndCustomerId(Long id, Long customerId);
}
