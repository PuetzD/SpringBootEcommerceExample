package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeScope;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.LinkedHashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "catalog_attribute_definition")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogAttributeDefinitionJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String code;

    @Column(nullable = false, length = 120)
    private String label;

    @Enumerated(EnumType.STRING)
    @Column(name = "value_type", nullable = false, length = 20)
    private CatalogAttributeType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CatalogAttributeScope scope;

    @Column(name = "is_option", nullable = false)
    private boolean option;

    @Column(nullable = false)
    private boolean active = true;

    @OneToMany(mappedBy = "definition", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<CatalogAttributeValueJpaEntity> allowedValues = new LinkedHashSet<>();

    static CatalogAttributeDefinitionJpaEntity create(
            String code,
            String label,
            CatalogAttributeType type,
            CatalogAttributeScope scope,
            boolean option) {
        var entity = new CatalogAttributeDefinitionJpaEntity();
        entity.code = code;
        entity.label = label;
        entity.type = type;
        entity.scope = scope;
        entity.option = option;
        return entity;
    }
}
