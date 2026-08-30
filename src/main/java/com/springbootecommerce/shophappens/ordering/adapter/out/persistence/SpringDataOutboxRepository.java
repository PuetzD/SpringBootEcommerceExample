package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

interface SpringDataOutboxRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {
    List<OutboxEventJpaEntity> findTop100ByPublishedAtIsNullOrderByCreatedAtAsc();
}
