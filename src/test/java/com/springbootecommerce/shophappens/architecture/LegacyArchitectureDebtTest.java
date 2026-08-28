package com.springbootecommerce.shophappens.architecture;

import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class LegacyArchitectureDebtTest {
    private static final Set<String> LEGACY_FRAMEWORK_COUPLED_DOMAIN_TYPES =
            Set.of(
                    "com.springbootecommerce.shophappens.account.domain.Account",
                    "com.springbootecommerce.shophappens.catalog.domain.Product",
                    "com.springbootecommerce.shophappens.catalog.domain.Category",
                    "com.springbootecommerce.shophappens.cart.domain.Cart",
                    "com.springbootecommerce.shophappens.cart.domain.CartItem",
                    "com.springbootecommerce.shophappens.cart.domain.Quantity",
                    "com.springbootecommerce.shophappens.sharedkernel.money.Money");

    @Test
    void frameworkCoupledDomainTypesMatchTheExplicitDebtList() {
        var classes = new ClassFileImporter().importPackages("com.springbootecommerce.shophappens");

        Set<String> actual =
                classes.stream()
                        .filter(this::isDomainOrSharedKernel)
                        .filter(this::dependsOnPersistenceFramework)
                        .map(JavaClass::getName)
                        .collect(Collectors.toSet());

        assertThat(actual)
                .containsExactlyInAnyOrderElementsOf(LEGACY_FRAMEWORK_COUPLED_DOMAIN_TYPES);
    }

    private boolean isDomainOrSharedKernel(JavaClass type) {
        return type.getPackageName().contains(".domain")
                || type.getPackageName().contains(".sharedkernel");
    }

    private boolean dependsOnPersistenceFramework(JavaClass type) {
        return type.getDirectDependenciesFromSelf().stream()
                .map(dependency -> dependency.getTargetClass().getPackageName())
                .anyMatch(
                        packageName ->
                                packageName.startsWith("jakarta.persistence")
                                        || packageName.startsWith("org.hibernate"));
    }
}
