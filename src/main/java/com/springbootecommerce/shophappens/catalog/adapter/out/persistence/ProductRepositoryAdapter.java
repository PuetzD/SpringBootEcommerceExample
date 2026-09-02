package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.DuplicateSkuException;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminPage;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductCategorySummary;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.ProductRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.StaleProductRevisionException;
import com.springbootecommerce.shophappens.catalog.application.port.out.ProductRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.VersionedProduct;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import com.springbootecommerce.shophappens.catalog.domain.model.Product;
import com.springbootecommerce.shophappens.catalog.domain.model.Sku;
import com.springbootecommerce.shophappens.sharedkernel.identity.ProductId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
class ProductRepositoryAdapter implements ProductRepository {
    private final SpringDataProductRepository springData;
    private final SpringDataCategoryRepository categories;
    private final CatalogPersistenceMapper mapper;

    @Override
    @Transactional(readOnly = true)
    public Optional<Product> findById(ProductId id) {
        return springData.findDetailedById(id.value()).map(mapper::toDomain);
    }

    @Override
    @Transactional
    public Optional<Product> findForPurchase(ProductId id) {
        return springData.findForPurchaseById(id.value()).map(mapper::toDomain);
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
    public long countActiveByCategoryId(CategoryId categoryId) {
        return springData.countActiveByCategoryId(categoryId.value());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findActiveByCategoryId(CategoryId categoryId) {
        return springData.findActiveByCategoryId(categoryId.value()).stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAllActive() {
        return springData.findByActiveTrueOrderByNameAscIdAsc().stream()
                .map(mapper::toDomain)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return springData.findAllByOrderByNameAscIdAsc().stream().map(mapper::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductAdminPage searchForAdministration(ProductAdminSearch search) {
        var page =
                springData.searchForAdministration(
                        search.query() == null ? "" : search.query(),
                        search.active(),
                        org.springframework.data.domain.PageRequest.of(
                                search.page(),
                                search.size(),
                                org.springframework.data.domain.Sort.by(
                                        org.springframework.data.domain.Sort.Direction.DESC,
                                        "id")));
        List<Long> ids = page.getContent().stream().map(ProductJpaEntity::getId).toList();
        Map<Long, ProductJpaEntity> detailed =
                ids.isEmpty()
                        ? Map.of()
                        : springData.findDetailedByIdIn(ids).stream()
                                .collect(
                                        Collectors.toMap(
                                                ProductJpaEntity::getId, Function.identity()));
        List<ProductAdminView> content =
                ids.stream().map(detailed::get).map(this::toAdminView).toList();
        return new ProductAdminPage(
                content,
                search.page(),
                search.size(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VersionedProduct> findForAdministration(ProductId id) {
        return springData
                .findDetailedById(id.value())
                .map(entity -> new VersionedProduct(mapper.toDomain(entity), entity.getVersion()));
    }

    @Override
    @Transactional
    public VersionedProduct insertForAdministration(Product product) {
        try {
            ProductJpaEntity entity = mapper.toJpa(product, loadCategories(product));
            ProductJpaEntity saved = springData.saveAndFlush(entity);
            return new VersionedProduct(mapper.toDomain(saved), saved.getVersion());
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateSkuException(product.sku().value());
        }
    }

    @Override
    @Transactional
    public VersionedProduct updateForAdministration(
            Product product, ProductRevision expectedRevision) {
        long id = product.id().orElseThrow().value();
        ProductJpaEntity entity =
                springData
                        .findDetailedById(id)
                        .orElseThrow(
                                () -> new IllegalArgumentException("Product not found: " + id));
        if (entity.getVersion() != expectedRevision.value()) {
            throw new StaleProductRevisionException(new ProductReference(id), expectedRevision);
        }
        try {
            mapper.applyToJpa(entity, product, loadCategories(product));
            ProductJpaEntity saved = springData.saveAndFlush(entity);
            return new VersionedProduct(mapper.toDomain(saved), saved.getVersion());
        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new StaleProductRevisionException(new ProductReference(id), expectedRevision);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateSkuException(product.sku().value());
        }
    }

    private Set<CategoryJpaEntity> loadCategories(Product product) {
        Set<Long> ids =
                product.categoryIds().stream().map(CategoryId::value).collect(Collectors.toSet());
        if (ids.isEmpty()) return Set.of();
        List<CategoryJpaEntity> found = categories.findAllById(ids);
        if (found.size() != ids.size()) {
            Set<Long> missing = new HashSet<>(ids);
            found.forEach(category -> missing.remove(category.getId()));
            throw new CategoryNotFoundException(new CategoryReference(missing.iterator().next()));
        }
        return Set.copyOf(found);
    }

    private ProductAdminView toAdminView(ProductJpaEntity entity) {
        Product product = mapper.toDomain(entity);
        Map<Long, CategoryJpaEntity> categoriesById =
                entity.getCategories().stream()
                        .collect(Collectors.toMap(CategoryJpaEntity::getId, Function.identity()));
        return new ProductAdminView(
                new ProductReference(entity.getId()),
                product.sku().value(),
                product.name(),
                product.description(),
                product.price(),
                product.stockQuantity(),
                product.imageUrl(),
                product.active(),
                new ProductRevision(entity.getVersion()),
                product.categoryIds().stream()
                        .map(
                                id -> {
                                    CategoryJpaEntity category = categoriesById.get(id.value());
                                    return new ProductCategorySummary(
                                            new CategoryReference(category.getId()),
                                            category.getName(),
                                            category.getSlug());
                                })
                        .toList());
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
