package com.springbootecommerce.demo.customer.persistence;

import com.springbootecommerce.demo.customer.domain.Address;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AddressRepository extends JpaRepository<Address, Long> {
  List<Address> findByCustomerIdOrderByDefaultShippingDesc(Long customerId);

  List<Address> findByCustomerIdAndDefaultShippingTrue(Long customerId);

  List<Address> findByCustomerIdAndDefaultBillingTrue(Long customerId);
}
