package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "catalog_attribute_allowed_value")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CatalogAttributeValueJpaEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "definition_id", nullable = false)
    private CatalogAttributeDefinitionJpaEntity definition;

    @Column(nullable = false, length = 80)
    private String code;

    @Column(nullable = false, length = 120)
    private String label;

    @Column(nullable = false)
    private boolean active = true;

    static CatalogAttributeValueJpaEntity create(
            CatalogAttributeDefinitionJpaEntity definition, String code, String label) {
        var entity = new CatalogAttributeValueJpaEntity();
        entity.definition = definition;
        entity.code = code;
        entity.label = label;
        return entity;
    }
}
