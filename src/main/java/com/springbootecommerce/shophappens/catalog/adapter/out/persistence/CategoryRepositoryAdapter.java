package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminPage;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminSearch;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryAdminView;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryInUseException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryNotFoundException;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryOption;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryReference;
import com.springbootecommerce.shophappens.catalog.application.port.in.CategoryRevision;
import com.springbootecommerce.shophappens.catalog.application.port.in.DuplicateCategoryException;
import com.springbootecommerce.shophappens.catalog.application.port.in.StaleCategoryRevisionException;
import com.springbootecommerce.shophappens.catalog.application.port.out.CategoryRepository;
import com.springbootecommerce.shophappens.catalog.application.port.out.VersionedCategory;
import com.springbootecommerce.shophappens.catalog.domain.model.Category;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Repository
class CategoryRepositoryAdapter implements CategoryRepository {
    private final SpringDataCategoryRepository springData;
    private final SpringDataProductRepository products;

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return springData.findAllByOrderByNameAscIdAsc().stream().map(this::toDomain).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findBySlug(String slug) {
        return springData.findBySlug(slug).map(this::toDomain);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryAdminPage searchForAdministration(CategoryAdminSearch search) {
        var page =
                springData.findAllBy(
                        org.springframework.data.domain.PageRequest.of(
                                search.page(),
                                search.size(),
                                org.springframework.data.domain.Sort.by("name")
                                        .ascending()
                                        .and(
                                                org.springframework.data.domain.Sort.by("id")
                                                        .ascending())));
        var ids =
                page.getContent().stream()
                        .map(CategoryJpaEntity::getId)
                        .collect(Collectors.toSet());
        Map<Long, Long> counts = membershipCounts(ids);
        return new CategoryAdminPage(
                page.getContent().stream()
                        .map(
                                category ->
                                        toAdminView(
                                                category,
                                                counts.getOrDefault(category.getId(), 0L)))
                        .toList(),
                search.page(),
                search.size(),
                page.getTotalElements(),
                page.getTotalPages());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CategoryOption> findOptionsForAdministration() {
        return springData.findAllByOrderByNameAscIdAsc().stream()
                .map(
                        category ->
                                new CategoryOption(
                                        new CategoryReference(category.getId()),
                                        category.getName(),
                                        category.getSlug()))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<VersionedCategory> findForAdministration(CategoryId id) {
        return springData
                .findById(id.value())
                .map(category -> new VersionedCategory(toDomain(category), category.getVersion()));
    }

    @Override
    @Transactional
    public VersionedCategory insertForAdministration(Category category) {
        try {
            CategoryJpaEntity entity = new CategoryJpaEntity();
            entity.setName(category.name());
            entity.setSlug(category.slug());
            CategoryJpaEntity saved = springData.saveAndFlush(entity);
            return new VersionedCategory(toDomain(saved), saved.getVersion());
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateCategoryException(category.name(), category.slug());
        }
    }

    @Override
    @Transactional
    public VersionedCategory updateForAdministration(
            Category category, CategoryRevision expectedRevision) {
        long id = category.id().orElseThrow().value();
        CategoryJpaEntity entity =
                springData
                        .findById(id)
                        .orElseThrow(
                                () -> new CategoryNotFoundException(new CategoryReference(id)));
        if (entity.getVersion() != expectedRevision.value()) {
            throw new StaleCategoryRevisionException(new CategoryReference(id), expectedRevision);
        }
        try {
            entity.setName(category.name());
            entity.setSlug(category.slug());
            CategoryJpaEntity saved = springData.saveAndFlush(entity);
            return new VersionedCategory(toDomain(saved), saved.getVersion());
        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new StaleCategoryRevisionException(new CategoryReference(id), expectedRevision);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateCategoryException(category.name(), category.slug());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countProductsForAdministration(CategoryId id) {
        return membershipCounts(Set.of(id.value())).getOrDefault(id.value(), 0L);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isReferencedByAnyProduct(CategoryId id) {
        return products.existsByCategoriesId(id.value());
    }

    @Override
    @Transactional
    public void deleteForAdministration(CategoryId id, CategoryRevision expectedRevision) {
        CategoryJpaEntity entity =
                springData
                        .findById(id.value())
                        .orElseThrow(
                                () ->
                                        new CategoryNotFoundException(
                                                new CategoryReference(id.value())));
        if (entity.getVersion() != expectedRevision.value()) {
            throw new StaleCategoryRevisionException(
                    new CategoryReference(id.value()), expectedRevision);
        }
        if (isReferencedByAnyProduct(id)) {
            throw new CategoryInUseException(new CategoryReference(id.value()));
        }
        try {
            springData.delete(entity);
            springData.flush();
        } catch (ObjectOptimisticLockingFailureException exception) {
            throw new StaleCategoryRevisionException(
                    new CategoryReference(id.value()), expectedRevision);
        } catch (DataIntegrityViolationException exception) {
            throw new CategoryInUseException(new CategoryReference(id.value()));
        }
    }

    private Map<Long, Long> membershipCounts(Set<Long> ids) {
        if (ids.isEmpty()) return Map.of();
        return products.countProductsByCategoryIds(ids).stream()
                .collect(
                        Collectors.toMap(
                                row -> ((Number) row[0]).longValue(),
                                row -> ((Number) row[1]).longValue()));
    }

    private CategoryAdminView toAdminView(CategoryJpaEntity category, long count) {
        return new CategoryAdminView(
                new CategoryReference(category.getId()),
                category.getName(),
                category.getSlug(),
                new CategoryRevision(category.getVersion()),
                count);
    }

    private Category toDomain(CategoryJpaEntity jpa) {
        return Category.restore(new CategoryId(jpa.getId()), jpa.getName(), jpa.getSlug());
    }
}
