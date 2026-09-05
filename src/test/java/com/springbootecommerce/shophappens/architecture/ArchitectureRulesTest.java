package com.springbootecommerce.shophappens.architecture;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ArchRule;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ArchitectureRulesTest {
    private static final String ROOT = "com.springbootecommerce.shophappens";
    private static final List<String> BOUNDED_CONTEXTS =
            List.of("account", "customer", "catalog", "cart", "ordering");
    // Extra cycle-detection slices for adapter and shared web code that is not a bounded
    // context. "sharedkernel" precedes "shared" so the longer prefix wins in sliceOf().
    private static final List<String> PROTECTED_SLICES =
            List.of(
                    "sharedkernel",
                    "configuration",
                    "storefront",
                    "security",
                    "administration",
                    "shared",
                    "account",
                    "customer",
                    "catalog",
                    "cart",
                    "ordering");
    private static JavaClasses imported;

    @BeforeAll
    static void importApplication() {
        imported =
                new ClassFileImporter()
                        .withImportOption(location -> location.contains("/target/classes/"))
                        .importPackages(ROOT);
    }

    @Test
    void importedClassesArePresent() {
        assertThat(imported).isNotEmpty();
    }

    @Test
    void applicationClassesBelongToKnownArchitecturalSlices() {
        List<String> violations = new java.util.ArrayList<>();
        for (JavaClass clazz : imported) {
            if (!clazz.getPackageName().startsWith(ROOT)) {
                continue;
            }
            boolean isKnownTopLevelSlice =
                    PROTECTED_SLICES.stream()
                            .map(context -> ROOT + "." + context)
                            .anyMatch(clazz.getPackageName()::equals);
            if (sliceOf(clazz) == null
                    && !clazz.getPackageName().equals(ROOT)
                    && !isKnownTopLevelSlice) {
                violations.add(clazz.getName() + " is outside all known architectural slices");
            }
        }
        assertThat(violations).isEmpty();
    }

    @Test
    void domainAndSharedKernelAreFrameworkFree() {
        noClasses()
                .that()
                .resideInAnyPackage("..domain..", "..sharedkernel..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "org.springframework.data.redis..",
                        "lombok..")
                .check(imported);
    }

    @Test
    void boundedContextDomainsDependOnlyOnOwnDomainSharedKernelAndJavaTypes() {
        for (String context : BOUNDED_CONTEXTS) {
            classes()
                    .that()
                    .resideInAPackage(ROOT + "." + context + ".domain..")
                    .should()
                    .onlyDependOnClassesThat()
                    .resideInAnyPackage(
                            ROOT + "." + context + ".domain..", ROOT + ".sharedkernel..", "java..")
                    .check(imported);
        }
    }

    @Test
    void sharedKernelDoesNotDependOnBoundedContextsFrameworksOrWebTypes() {
        noClasses()
                .that()
                .resideInAPackage(ROOT + ".sharedkernel..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        ROOT + ".account..",
                        ROOT + ".customer..",
                        ROOT + ".catalog..",
                        ROOT + ".cart..",
                        ROOT + ".ordering..",
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "jakarta.servlet..",
                        "jakarta.ws.rs..",
                        "org.thymeleaf..",
                        "..web..")
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
                .check(imported);
    }

    @Test
    void contextsCannotReachAnotherContextsInternals() {
        for (String context : BOUNDED_CONTEXTS) {
            ArchRule rule =
                    noClasses()
                            .that()
                            .resideOutsideOfPackage(ROOT + "." + context + "..")
                            .should()
                            .dependOnClassesThat()
                            .resideInAnyPackage(
                                    ".." + context + ".domain..",
                                    ".." + context + ".application.service..",
                                    ".." + context + ".application.port.out..",
                                    ".." + context + ".adapter..",
                                    ".." + context + ".web..");
            rule.check(imported);
        }
    }

    @Test
    void securityInternalReachIsBlocked() {
        noClasses()
                .that()
                .resideOutsideOfPackage("..security..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..security.web..", "..security..")
                .check(imported);
    }

    @Test
    void storefrontInternalReachIsBlocked() {
        noClasses()
                .that()
                .resideOutsideOfPackage("..storefront..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..storefront..")
                .check(imported);
    }

    @Test
    void administrationInternalReachIsBlocked() {
        noClasses()
                .that()
                .resideOutsideOfPackage("..administration..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..administration..")
                .check(imported);
    }

    @Test
    void sharedWebSupportDoesNotDependOnContextInternals() {
        noClasses()
                .that()
                .resideInAPackage("..shared.web..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "..account..",
                        "..customer..",
                        "..catalog..",
                        "..cart..",
                        "..ordering..",
                        "..security..",
                        "..storefront..")
                .check(imported);
    }

    @Test
    void crossContextDependenciesDoNotFormCycles() {
        Map<String, Set<String>> graph = new HashMap<>();
        for (String context : PROTECTED_SLICES) {
            graph.put(context, new HashSet<>());
        }
        for (JavaClass clazz : imported) {
            String from = sliceOf(clazz);
            if (from == null) {
                continue;
            }
            for (var dep : clazz.getDirectDependenciesFromSelf()) {
                JavaClass dependency = dep.getTargetClass();
                String to = sliceOf(dependency);
                if (to == null || from.equals(to)) {
                    continue;
                }
                graph.get(from).add(to);
            }
        }
        assertThatNoCycles(graph);
    }

    @Test
    void crossContextDependenciesUseOnlyPublishedContractsOrSharedKernel() {
        List<String> violations = new java.util.ArrayList<>();
        for (JavaClass clazz : imported) {
            String from = sliceOf(clazz);
            if (from == null || !BOUNDED_CONTEXTS.contains(from)) {
                continue;
            }
            for (var dependency : clazz.getDirectDependenciesFromSelf()) {
                JavaClass target = dependency.getTargetClass();
                String to = sliceOf(target);
                if (to == null
                        || to.equals(from)
                        || to.equals("sharedkernel")
                        || !BOUNDED_CONTEXTS.contains(to)) {
                    continue;
                }
                if (!target.getPackageName().endsWith(".application.port.in")) {
                    violations.add(
                            clazz.getName()
                                    + " -> "
                                    + target.getName()
                                    + " (slice "
                                    + from
                                    + " -> "
                                    + to
                                    + ", not application.port.in)");
                }
            }
        }
        assertThat(violations).isEmpty();
    }

    @Test
    void publishedContractsDoNotDependOnAnotherContextContractOrDomain() {
        List<String> violations = new java.util.ArrayList<>();
        for (JavaClass clazz : imported) {
            String from = publishedContextOf(clazz);
            if (from == null) {
                continue;
            }
            for (var dependency : clazz.getDirectDependenciesFromSelf()) {
                JavaClass target = dependency.getTargetClass();
                String to = sliceOf(target);
                if (to == null || to.equals(from) || to.equals("sharedkernel")) {
                    continue;
                }
                if (BOUNDED_CONTEXTS.contains(to)) {
                    violations.add(clazz.getName() + " -> " + target.getName());
                }
            }
        }
        assertThat(violations).isEmpty();
    }

    @Test
    void publishedInputPortsDoNotExposeDomainTypes() {
        noClasses()
                .that()
                .resideInAPackage("..application.port.in..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..domain..", "..adapter..", "..web..")
                .check(imported);
    }

    @Test
    void administrationUsesOnlyCatalogPublishedInputPorts() {
        noClasses()
                .that()
                .resideInAnyPackage("..administration..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage(
                        "..catalog.application.service..",
                        "..catalog.application.port.out..",
                        "..catalog.domain..",
                        "..catalog.adapter..")
                .check(imported);
    }

    @Test
    void publishedQueryContractsDoNotExposeDomainOrPersistenceTypes() {
        List<String> violations = new java.util.ArrayList<>();
        for (JavaClass clazz : imported) {
            if (!clazz.getPackageName().contains(".application.port.in")) {
                continue;
            }
            if (clazz.getSimpleName().endsWith("Exception")) {
                continue;
            }
            for (var dependency : clazz.getDirectDependenciesFromSelf()) {
                JavaClass target = dependency.getTargetClass();
                if (target.getPackageName().contains(".domain.")
                        || target.getPackageName().contains(".adapter.out.persistence")
                        || target.getPackageName().startsWith("jakarta.persistence.")) {
                    violations.add(clazz.getName() + " -> " + target.getName());
                }
            }
        }
        assertThat(violations).isEmpty();
    }

    @Test
    void inboundWebAdaptersDoNotUseAnotherContextsDomainOrExceptionTypes() {
        List<String> violations = new java.util.ArrayList<>();
        for (JavaClass clazz : imported) {
            String from = sliceOf(clazz);
            if (from == null
                    || !BOUNDED_CONTEXTS.contains(from)
                    || !clazz.getPackageName().contains(".adapter.in.web")) {
                continue;
            }
            for (var dependency : clazz.getDirectDependenciesFromSelf()) {
                JavaClass target = dependency.getTargetClass();
                String to = sliceOf(target);
                if (to == null || from.equals(to) || !BOUNDED_CONTEXTS.contains(to)) {
                    continue;
                }
                if (target.getPackageName().contains(".domain.")
                        || target.getSimpleName().endsWith("Exception")) {
                    violations.add(clazz.getName() + " -> " + target.getName());
                }
            }
        }
        assertThat(violations).isEmpty();
    }

    @Test
    void domainDoesNotDependOnAdapters() {
        noClasses()
                .that()
                .resideInAnyPackage("..domain..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..adapter..")
                .check(imported);
    }

    @Test
    void applicationDoesNotDependOnInfrastructure() {
        noClasses()
                .that()
                .resideInAnyPackage("..application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..adapter..", "..web..", "..security..", "..storefront..")
                .check(imported);
    }

    @Test
    void webAndAdaptersDoNotDependOnApplicationServices() {
        noClasses()
                .that()
                .resideInAnyPackage("..web..", "..adapter..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("..application.service..")
                .check(imported);
    }

    private static String sliceOf(JavaClass clazz) {
        for (String context : PROTECTED_SLICES) {
            if (clazz.getPackageName().startsWith(ROOT + "." + context + ".")) {
                return context;
            }
        }
        return null;
    }

    private static String publishedContextOf(JavaClass clazz) {
        if (!clazz.getPackageName().contains(".application.port.in")) {
            return null;
        }
        String slice = sliceOf(clazz);
        return BOUNDED_CONTEXTS.contains(slice) ? slice : null;
    }

    private static void assertThatNoCycles(Map<String, Set<String>> graph) {
        Map<String, Integer> inDegree = new HashMap<>();
        for (String node : graph.keySet()) {
            inDegree.put(node, 0);
        }
        for (Set<String> targets : graph.values()) {
            for (String target : targets) {
                inDegree.merge(target, 1, Integer::sum);
            }
        }
        Deque<String> queue = new ArrayDeque<>();
        for (Map.Entry<String, Integer> entry : inDegree.entrySet()) {
            if (entry.getValue() == 0) {
                queue.add(entry.getKey());
            }
        }
        int processed = 0;
        while (!queue.isEmpty()) {
            String node = queue.poll();
            processed++;
            for (String next : graph.get(node)) {
                if (inDegree.merge(next, -1, Integer::sum) == 0) {
                    queue.add(next);
                }
            }
        }
        assertThat(processed)
                .withFailMessage("Architectural dependency cycle detected: %s", graph)
                .isEqualTo(graph.size());
    }
}
