package com.springbootecommerce.shophappens.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import java.util.List;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ArchitectureRulesTest {
    private static final String ROOT = "com.springbootecommerce.shophappens";
    private static final List<String> CONTEXTS =
            List.of("account", "customer", "catalog", "cart", "ordering");
    private static JavaClasses imported;

    @BeforeAll
    static void importApplication() {
        imported =
                new ClassFileImporter()
                        .withImportOption(location -> location.contains("/target/classes/"))
                        .importPackages(ROOT);
    }

    @Test
    void migratedDomainsAreFrameworkFree() {
        noClasses()
                .that()
                .resideInAPackage("..domain.model..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "org.springframework.data.redis..")
                .allowEmptyShould(true)
                .check(imported);
    }

    @Test
    void applicationPortsAreFrameworkFree() {
        noClasses()
                .that()
                .resideInAPackage("..application.port..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..", "jakarta.persistence..", "org.hibernate..")
                .allowEmptyShould(true)
                .check(imported);
    }

    @Test
    void contextsCannotReachAnotherContextsInternals() {
        for (String context : CONTEXTS) {
            ArchRule rule =
                    noClasses()
                            .that()
                            .resideOutsideOfPackage(".." + context + "..")
                            .should()
                            .dependOnClassesThat()
                            .resideInAnyPackage(
                                    ".." + context + ".domain..",
                                    ".." + context + ".application.service..",
                                    ".." + context + ".application.port.out..",
                                    ".." + context + ".adapter..");
            rule.allowEmptyShould(true).check(imported);
        }
    }

    @Test
    void featureSlicesAreFreeOfCycles() {
        slices().matching(ROOT + ".(*)..")
                .should()
                .beFreeOfCycles()
                .allowEmptyShould(true)
                .check(imported);
    }
}
