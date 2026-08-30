package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import com.springbootecommerce.shophappens.catalog.application.port.out.CategoryRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.Category;
import com.springbootecommerce.shophappens.catalog.domain.model.CategoryId;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
class CategoryRepositoryAdapter implements CategoryRepository {
    private final SpringDataCategoryRepository springData;

    CategoryRepositoryAdapter(SpringDataCategoryRepository springData) {
        this.springData = springData;
    }

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

    private Category toDomain(CategoryJpaEntity jpa) {
        return Category.restore(new CategoryId(jpa.getId()), jpa.getName());
    }
}
