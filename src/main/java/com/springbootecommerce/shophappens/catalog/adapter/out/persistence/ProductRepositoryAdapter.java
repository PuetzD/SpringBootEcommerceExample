package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class ProductRepositoryAdapter implements ProductRepository {
    private final SpringDataProductRepository springData;
    private final SpringDataCategoryRepository categories;
    private final CatalogPersistenceMapper mapper;

    ProductRepositoryAdapter(
            SpringDataProductRepository springData,
            SpringDataCategoryRepository categories,
            CatalogPersistenceMapper mapper) {
        this.springData = springData;
        this.categories = categories;
        this.mapper = mapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(ProductId id) {
        return springData.findDetailedById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findActiveById(ProductId id) {
        return springData.findByIdAndActiveTrue(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findActiveBySku(Sku sku) {
        return springData.findBySkuAndActiveTrue(sku.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAllActive() {
        return springData.findByActiveTrueOrderByNameAscIdAsc().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional
    public Product save(Product product) {
        Set<CategoryJpaEntity> categoryRefs = loadCategoryRefs(product.categoryIds());
        ProductJpaEntity jpa = toJpaForSave(product, categoryRefs);
        return mapper.toDomain(springData.save(jpa));
    }

    private ProductJpaEntity toJpaForSave(Product product, Set<CategoryJpaEntity> categories) {
        return product.id()
                .map(id -> mergeForUpdate(id.value(), product, categories))
                .orElseGet(() -> mapper.toJpa(product, categories));
    }

    private ProductJpaEntity mergeForUpdate(
            long id, Product product, Set<CategoryJpaEntity> categories) {
        ProductJpaEntity existing =
                springData
                        .findDetailedById(id)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Product not found for update: " + id));
        mapper.applyToJpa(existing, product, categories);
        return existing;
    }

    private Set<CategoryJpaEntity> loadCategoryRefs(Set<CategoryId> ids) {
        Set<Long> idValues = ids.stream().map(CategoryId::value).collect(Collectors.toSet());
        if (idValues.isEmpty()) {
            return Set.of();
        }
        List<CategoryJpaEntity> found = categories.findAllById(idValues);
        if (found.size() != idValues.size()) {
            Set<Long> missing = new HashSet<>(idValues);
            found.forEach(category -> missing.remove(category.getId()));
            throw new IllegalArgumentException("Unknown category IDs: " + missing);
        }
        return Set.copyOf(found);
    }
}
