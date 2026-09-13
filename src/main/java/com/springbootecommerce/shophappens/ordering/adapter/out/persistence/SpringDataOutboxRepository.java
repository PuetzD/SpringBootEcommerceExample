package com.springbootecommerce.shophappens.ordering.adapter.out.persistence;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataOutboxRepository extends JpaRepository<OutboxEventJpaEntity, UUID> {
    @Query(
            "select e from OutboxEventJpaEntity e where e.publishedAt is null "
                    + "and e.quarantinedAt is null and e.nextAttemptAt <= :now "
                    + "order by e.nextAttemptAt, e.createdAt, e.eventId")
    List<OutboxEventJpaEntity> findEligible(@Param("now") Instant now, Pageable pageable);
}
