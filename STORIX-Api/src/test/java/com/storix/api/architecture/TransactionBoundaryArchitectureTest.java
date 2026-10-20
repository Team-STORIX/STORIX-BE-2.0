package com.storix.api.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.domain.JavaMethod;
import com.tngtech.archunit.core.domain.JavaMethodCall;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;

class TransactionBoundaryArchitectureTest {

    // 트랜잭션 안에서 기다리면 그동안 DB 커넥션을 쥐고 있게 되는 바깥 호출
    // ES 는 쿼리 조립 클래스는 네트워크를 안 타서 클라이언트만 본다
    private static final List<String> OUTSIDE_PACKAGES = List.of(
            "org.springframework.data.redis.core",
            "org.springframework.data.elasticsearch.core",
            "org.springframework.web.client",
            "org.springframework.web.reactive.function.client",
            "com.google.firebase",
            "software.amazon.awssdk"
    );
    private static final List<String> OUTSIDE_CLASSES = List.of(
            "co.elastic.clients.elasticsearch.ElasticsearchClient"
    );
    private static final String FEIGN_CLIENT = "org.springframework.cloud.openfeign.FeignClient";
    private static final int MAX_DEPTH = 15;

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.storix");
    }

    @Test
    @DisplayName("트랜잭션 안에서 Redis · ES · 외부 API 를 부르지 않는다. 호출을 끝까지 따라가 본다")
    void transactionDoesNotCallOutside() {
        ArchRule rule = methods().that().areAnnotatedWith(Transactional.class)
                .or().areDeclaredInClassesThat().areAnnotatedWith(Transactional.class)
                .should(notReachOutsideCalls());
        rule.check(classes);
    }

    private static ArchCondition<JavaMethod> notReachOutsideCalls() {
        return new ArchCondition<>("트랜잭션 안에서 Redis · ES · 외부 API 를 부르지 않는다") {
            @Override
            public void check(JavaMethod method, ConditionEvents events) {
                List<String> path = new ArrayList<>();
                path.add(name(method));
                walk(method, path, new HashSet<>(), new HashSet<>(), method, events);
            }
        };
    }

    // 같은 시작 메서드에서 같은 바깥 클래스는 처음 찾은 경로 하나만 보고한다
    private static void walk(JavaCodeUnit unit, List<String> path, Set<JavaCodeUnit> visited, Set<String> reported,
                             JavaMethod start, ConditionEvents events) {
        if (!visited.add(unit) || path.size() > MAX_DEPTH) {
            return;
        }
        // 실행마다 같은 경로가 나오게 줄 번호 순으로 따라간다
        List<JavaMethodCall> calls = unit.getMethodCallsFromSelf().stream()
                .sorted(Comparator.comparingInt(JavaMethodCall::getLineNumber).thenComparing(c -> c.getTarget().getFullName()))
                .toList();
        for (JavaMethodCall call : calls) {
            JavaClass owner = call.getTargetOwner();
            if (isOutside(owner)) {
                if (!reported.add(owner.getName())) {
                    continue;
                }
                events.add(SimpleConditionEvent.violated(start,
                        String.join(" → ", path) + " → " + owner.getSimpleName()));
                continue;
            }
            if (!owner.getPackageName().startsWith("com.storix")) {
                continue;
            }
            for (JavaCodeUnit next : implementations(call)) {
                path.add(name(next));
                walk(next, path, visited, reported, start, events);
                path.remove(path.size() - 1);
            }
        }
    }

    // 인터페이스 · 상위 클래스로 부른 경우 실제 구현 메서드까지 따라간다
    private static List<JavaCodeUnit> implementations(JavaMethodCall call) {
        Optional<? extends JavaCodeUnit> declared = call.getTarget().resolveMember();
        if (declared.isEmpty()) {
            return List.of();
        }
        JavaCodeUnit target = declared.get();
        List<String> params = target.getRawParameterTypes().stream().map(JavaClass::getName).toList();
        return Stream.concat(Stream.of(target), target.getOwner().getAllSubclasses().stream()
                        .flatMap(sub -> sub.getMethods().stream())
                        .filter(m -> m.getName().equals(target.getName()))
                        .filter(m -> m.getRawParameterTypes().stream().map(JavaClass::getName).toList().equals(params)))
                .map(JavaCodeUnit.class::cast)
                .sorted(Comparator.comparing(JavaCodeUnit::getFullName))
                .toList();
    }

    private static boolean isOutside(JavaClass owner) {
        return OUTSIDE_PACKAGES.stream().anyMatch(owner.getPackageName()::startsWith)
                || OUTSIDE_CLASSES.contains(owner.getName())
                || owner.isAnnotatedWith(FEIGN_CLIENT);
    }

    private static String name(JavaCodeUnit unit) {
        return unit.getOwner().getSimpleName() + "." + unit.getName();
    }
}
