package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

interface SpringDataProductRepository extends JpaRepository<ProductJpaEntity, Long> {
    @EntityGraph(attributePaths = "categories")
    Optional<ProductJpaEntity> findDetailedById(Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from ProductJpaEntity p where p.id = :id")
    Optional<ProductJpaEntity> findForPurchaseById(@Param("id") Long id);

    @EntityGraph(attributePaths = "categories")
    Optional<ProductJpaEntity> findByIdAndActiveTrue(Long id);

    @EntityGraph(attributePaths = "categories")
    Optional<ProductJpaEntity> findBySkuAndActiveTrue(String sku);

    @EntityGraph(attributePaths = "categories")
    List<ProductJpaEntity> findByActiveTrueOrderByNameAscIdAsc();

    @EntityGraph(attributePaths = "categories")
    org.springframework.data.domain.Page<ProductJpaEntity> findByActiveTrue(
            org.springframework.data.domain.Pageable pageable);

    @EntityGraph(attributePaths = "categories")
    List<ProductJpaEntity> findAllByOrderByNameAscIdAsc();

    @Query(
            """
                    select p from ProductJpaEntity p
                    where (:active is null or p.active = :active)
                      and (:query = '' or lower(p.sku) like lower(concat('%', :query, '%'))
                           or lower(p.name) like lower(concat('%', :query, '%')))
                    """)
    org.springframework.data.domain.Page<ProductJpaEntity> searchForAdministration(
            @Param("query") String query,
            @Param("active") Boolean active,
            org.springframework.data.domain.Pageable pageable);

    @EntityGraph(attributePaths = "categories")
    @Query("select distinct p from ProductJpaEntity p where p.id in :ids")
    List<ProductJpaEntity> findDetailedByIdIn(@Param("ids") List<Long> ids);

    @Query(
            "select count(distinct p) from ProductJpaEntity p join p.categories c where c.id = :categoryId and p.active = true")
    long countActiveByCategoryId(Long categoryId);

    @Query(
            "select distinct p from ProductJpaEntity p join p.categories c where c.id = :categoryId and p.active = true order by p.name asc, p.id asc")
    List<ProductJpaEntity> findActiveByCategoryId(Long categoryId);

    @Query(
            """
                    select c.id, count(p.id)
                    from ProductJpaEntity p join p.categories c
                    where c.id in :categoryIds
                    group by c.id
                    """)
    List<Object[]> countProductsByCategoryIds(
            @Param("categoryIds") java.util.Set<Long> categoryIds);

    boolean existsByCategoriesId(Long categoryId);
}
