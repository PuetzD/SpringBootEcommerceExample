package com.springbootecommerce.shophappens.customer.persistence;

import com.springbootecommerce.shophappens.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {}
