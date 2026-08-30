package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataCategoryRepository extends JpaRepository<CategoryJpaEntity, Long> {
    Optional<CategoryJpaEntity> findBySlug(String slug);

    List<CategoryJpaEntity> findAllByOrderByNameAscIdAsc();
}
