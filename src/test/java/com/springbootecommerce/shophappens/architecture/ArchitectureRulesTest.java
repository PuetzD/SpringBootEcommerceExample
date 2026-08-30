package com.springbootecommerce.shophappens.architecture;

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
                        "org.springframework.data.redis..")
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
                // Cross-context published-contract usage (another context's application.port.in)
                // is the spec-sanctioned collaboration channel and is excluded from the cycle
                // check.
                if (dependency.getPackageName().endsWith(".application.port.in")) {
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
                .resideInAnyPackage(
                        "..adapter..",
                        "..web..",
                        "..security..",
                        "..storefront..",
                        "..administration..")
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
