package com.springbootecommerce.shophappens.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import org.junit.jupiter.api.Test;

class ArchitectureRulesTest {
    private static final String ROOT = "com.springbootecommerce.shophappens";

    @Test
    void importsTheApplicationForArchitectureChecks() {
        JavaClasses imported = new ClassFileImporter().importPackages(ROOT);
        classes().should().resideInAPackage(ROOT + "..").check(imported);
    }
}