package com.springbootecommerce.demo.customer.persistence;

import com.springbootecommerce.demo.customer.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {}
