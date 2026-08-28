package com.springbootecommerce.shophappens.cart.persistence;

import com.springbootecommerce.shophappens.cart.domain.Cart;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CartRepository extends JpaRepository<Cart, Long> {

    @Modifying
    @Query(
            value =
                    """
                    INSERT INTO cart (customer_id)
                    VALUES (:customerId)
                    ON CONFLICT (customer_id) DO NOTHING
                    """,
            nativeQuery = true)
    int ensureExistsForCustomer(@Param("customerId") Long customerId);

    @EntityGraph(attributePaths = "items")
    Optional<Cart> findByCustomerId(Long customerId);
}
