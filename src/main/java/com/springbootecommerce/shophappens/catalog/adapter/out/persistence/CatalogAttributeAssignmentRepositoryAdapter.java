package com.springbootecommerce.shophappens.catalog.adapter.out.persistence;

import com.springbootecommerce.shophappens.catalog.application.port.out.CatalogAttributeAssignmentRepository;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeAssignment;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeScope;
import com.springbootecommerce.shophappens.catalog.domain.model.CatalogAttributeType;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
class CatalogAttributeAssignmentRepositoryAdapter implements CatalogAttributeAssignmentRepository {
    private final JdbcTemplate jdbc;

    @Override
    @Transactional
    public void save(CatalogAttributeAssignment assignment) {
        String table =
                assignment.scope() == CatalogAttributeScope.PRODUCT
                        ? "product_attribute_assignment"
                        : "product_variant_attribute_assignment";
        String ownerColumn =
                assignment.scope() == CatalogAttributeScope.PRODUCT ? "product_id" : "variant_id";
        String valueColumn = valueColumn(assignment);
        String valueExpression =
                valueColumn.equals("select_value_id")
                        ? "(select av.id from catalog_attribute_allowed_value av "
                                + "join catalog_attribute_definition d on d.id = av.definition_id "
                                + "where d.code = ? and av.code = ?)"
                        : "?";
        String sql =
                "insert into "
                        + table
                        + " ("
                        + ownerColumn
                        + ", definition_id, "
                        + valueColumn
                        + ") select ?, d.id, "
                        + valueExpression
                        + " from catalog_attribute_definition d where d.code = ?";
        Object[] parameters =
                valueColumn.equals("select_value_id")
                        ? new Object[] {
                            assignment.ownerId(),
                            assignment.definitionCode(),
                            assignment.value(),
                            assignment.definitionCode()
                        }
                        : new Object[] {
                            assignment.ownerId(),
                            typedValue(assignment),
                            assignment.definitionCode()
                        };
        if (jdbc.update(sql, parameters) != 1) {
            throw new IllegalArgumentException("Unknown or inactive attribute definition");
        }
    }

    private String valueColumn(CatalogAttributeAssignment assignment) {
        return jdbc.queryForObject(
                "select case value_type when 'TEXT' then 'text_value' when 'NUMBER' then 'number_value' "
                        + "when 'BOOLEAN' then 'boolean_value' when 'SELECT' then 'select_value_id' end "
                        + "from catalog_attribute_definition where code = ?",
                String.class,
                assignment.definitionCode());
    }

    private Object typedValue(CatalogAttributeAssignment assignment) {
        String type =
                jdbc.queryForObject(
                        "select value_type from catalog_attribute_definition where code = ?",
                        String.class,
                        assignment.definitionCode());
        return switch (CatalogAttributeType.valueOf(type)) {
            case TEXT -> assignment.value();
            case NUMBER -> new BigDecimal(assignment.value());
            case BOOLEAN -> Boolean.valueOf(assignment.value());
            case SELECT -> throw new IllegalArgumentException("SELECT requires a controlled value");
        };
    }
}
