package com.springbootecommerce.shophappens.ordering.notification.adapter.out.persistence;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataOrderConfirmationDeliveryRepository
        extends JpaRepository<OrderConfirmationDeliveryJpaEntity, UUID> {}
