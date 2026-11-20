package com.storix.api.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModule;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;
import org.yaml.snakeyaml.Yaml;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[컨벤션] 도메인 경계")
class DomainBoundaryArchitectureTest {

    private static final String DOMAIN_PACKAGE = "com.storix.domain.domains";
    private static final String REGISTRY = "agents/domains.yaml";
    private static final String IMPACT_DOC = "agents/domain-impact.md";

    private static ApplicationModules modules;
    private static Path root;
    private static Map<String, Set<String>> codeDependencies;
    private static Map<String, String> descriptions;
    private static Map<String, Set<String>> registeredDependencies;

    @BeforeAll
    static void load() throws IOException {
        modules = ApplicationModules.of(DOMAIN_PACKAGE, new ImportOption.DoNotIncludeTests());
        root = findRoot();

        codeDependencies = new TreeMap<>();
        modules.stream().forEach(module -> codeDependencies.put(module.getName(), module.getDirectDependencies(modules)
                .uniqueModules()
                .map(ApplicationModule::getName)
                .filter(target -> !target.equals(module.getName()))
                .collect(Collectors.toCollection(TreeSet::new))));

        descriptions = new TreeMap<>();
        registeredDependencies = new TreeMap<>();
        try (Reader reader = Files.newBufferedReader(root.resolve(REGISTRY))) {
            Map<String, Map<String, Object>> domains = new Yaml().<Map<String, Map<String, Map<String, Object>>>>load(reader).get("domains");
            domains.forEach((name, spec) -> {
                descriptions.put(name, String.valueOf(spec.getOrDefault("description", "")));
                Object dependsOn = spec.get("dependsOn");
                registeredDependencies.put(name, dependsOn == null
                        ? new TreeSet<>()
                        : ((List<?>) dependsOn).stream().map(String::valueOf).collect(Collectors.toCollection(TreeSet::new)));
            });
        }
    }

    @Test
    @DisplayName("코드에 있는 도메인은 모두 agents/domains.yaml 에 등록한다")
    void everyDomainIsRegistered() {
        Set<String> unregistered = new TreeSet<>(codeDependencies.keySet());
        unregistered.removeAll(registeredDependencies.keySet());

        assertThat(unregistered)
                .as("agents/domains.yaml 에 없는 도메인. description 과 dependsOn 을 적어 등록하세요")
                .isEmpty();
    }

    @Test
    @DisplayName("도메인 간 새 의존은 agents/domains.yaml 의 dependsOn 에 먼저 적는다")
    void noUnregisteredDependency() {
        List<String> added = new ArrayList<>();
        codeDependencies.forEach((domain, targets) -> targets.stream()
                .filter(target -> !registeredDependencies.getOrDefault(domain, Set.of()).contains(target))
                .forEach(target -> added.add(domain + " -> " + target)));

        assertThat(added)
                .as("dependsOn 에 없는 도메인 의존. 정말 필요한 의존이면 dependsOn 에 추가하고, 아니면 UseCase 에서 조합하거나 ID 로만 참조하세요")
                .isEmpty();
    }

    @Test
    @DisplayName("코드에서 사라진 의존은 agents/domains.yaml 에서도 지운다")
    void noStaleDependency() {
        List<String> stale = new ArrayList<>();
        codeDependencies.forEach((domain, targets) -> registeredDependencies.getOrDefault(domain, Set.of()).stream()
                .filter(target -> !targets.contains(target))
                .forEach(target -> stale.add(domain + " -> " + target)));

        assertThat(stale)
                .as("코드에서는 더 이상 쓰지 않는 의존. dependsOn 에서 지워 기준선을 줄이세요")
                .isEmpty();
    }

    @Test
    @DisplayName("agents/domain-impact.md 는 테스트가 만든 최신 상태로 둔다")
    void impactDocIsUpToDate() throws IOException {
        Path doc = root.resolve(IMPACT_DOC);
        String expected = renderImpactDoc();

        if ("true".equals(System.getenv("CI"))) {
            assertThat(Files.exists(doc) ? Files.readString(doc) : "")
                    .as("agents/domain-impact.md 가 최신이 아닙니다. 로컬에서 이 테스트를 돌려 다시 만든 뒤 커밋하세요")
                    .isEqualTo(expected);
            return;
        }
        Files.writeString(doc, expected);
    }

    @Test
    @DisplayName("도메인별 캔버스 · PlantUML 을 build/spring-modulith-docs 에 만든다")
    void writeModuleDocs() {
        new Documenter(modules)
                .writeModulesAsPlantUml()
                .writeIndividualModulesAsPlantUml()
                .writeModuleCanvases(Documenter.CanvasOptions.defaults().revealInternals());
    }

    private static String renderImpactDoc() {
        Map<String, Set<String>> graph = new TreeMap<>();
        Set<String> planned = new TreeSet<>(registeredDependencies.keySet());
        planned.removeAll(codeDependencies.keySet());
        registeredDependencies.keySet().forEach(domain -> graph.put(domain, codeDependencies.containsKey(domain)
                ? codeDependencies.get(domain)
                : registeredDependencies.get(domain)));

        Map<String, Set<String>> dependents = new TreeMap<>();
        graph.keySet().forEach(domain -> dependents.put(domain, new TreeSet<>()));
        graph.forEach((domain, targets) -> targets.forEach(target -> dependents.computeIfAbsent(target, key -> new TreeSet<>()).add(domain)));

        StringBuilder doc = new StringBuilder()
                .append("# 도메인 영향 범위\n\n")
                .append("> `DomainBoundaryArchitectureTest` 가 코드와 `agents/domains.yaml` 로 만드는 파일입니다. 직접 고치지 말고 테스트를 다시 돌리세요.\n")
                .append("> 기획 중인 도메인(코드 없이 등록만 한 것)은 yaml 의 dependsOn 으로 계산하고 그래프에서 점선으로 그립니다.\n\n")
                .append("| 도메인 | 설명 | 의존하는 도메인 | 나를 직접 쓰는 도메인 | 영향 범위(간접 포함) |\n")
                .append("| --- | --- | --- | --- | --- |\n");
        graph.keySet().forEach(domain -> doc.append("| ").append(label(domain, planned))
                .append(" | ").append(descriptions.getOrDefault(domain, ""))
                .append(" | ").append(graph.get(domain).size())
                .append(" | ").append(dependents.get(domain).size())
                .append(" | ").append(impacted(domain, dependents).size())
                .append(" |\n"));

        graph.keySet().forEach(domain -> {
            Set<String> direct = dependents.get(domain);
            Set<String> all = impacted(domain, dependents);
            doc.append("\n## ").append(label(domain, planned)).append("\n\n")
                    .append(descriptions.getOrDefault(domain, "")).append("\n\n")
                    .append("- 의존: ").append(join(graph.get(domain))).append("\n")
                    .append("- 나를 직접 쓰는 도메인: ").append(join(direct)).append("\n")
                    .append("- 바꾸면 영향 가는 도메인: ").append(join(all)).append("\n");
            if (!direct.isEmpty()) {
                doc.append("\n```mermaid\ngraph LR\n");
                direct.forEach(source -> doc.append("  ").append(source)
                        .append(planned.contains(source) ? " -.-> " : " --> ")
                        .append(domain).append("\n"));
                doc.append("```\n");
            }
        });
        return doc.toString();
    }

    private static Set<String> impacted(String domain, Map<String, Set<String>> dependents) {
        Set<String> visited = new TreeSet<>();
        Deque<String> queue = new ArrayDeque<>(dependents.getOrDefault(domain, Set.of()));
        while (!queue.isEmpty()) {
            String next = queue.poll();
            if (!next.equals(domain) && visited.add(next)) {
                queue.addAll(dependents.getOrDefault(next, Set.of()));
            }
        }
        return visited;
    }

    private static String label(String domain, Set<String> planned) {
        return planned.contains(domain) ? domain + " (기획 중)" : domain;
    }

    private static String join(Set<String> names) {
        return names.isEmpty() ? "없음" : String.join(", ", names);
    }

    private static Path findRoot() {
        Path dir = Path.of("").toAbsolutePath();
        while (dir != null && !Files.exists(dir.resolve("settings.gradle"))) {
            dir = dir.getParent();
        }
        return dir;
    }
}
