package com.storix.api.architecture;

import com.storix.common.annotation.UseCase;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaModifier;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

@DisplayName("[컨벤션] 계층 · 예외 규칙")
class ConventionArchitectureTest {

    private static JavaClasses classes;

    @BeforeAll
    static void importClasses() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.storix");
    }

    @Test
    @DisplayName("Service 는 Repository 를 직접 쓰지 않고 Adaptor 를 거친다")
    void serviceDoesNotUseRepository() {
        ArchRule rule = noClasses().that().resideInAPackage("..domains..service..").and().haveSimpleNameEndingWith("Service")
                .should().dependOnClassesThat().haveSimpleNameEndingWith("Repository");
        rule.check(classes);
    }

    @Test
    @DisplayName("Service 는 다른 Service 를 부르지 않는다. 여러 Service 조합은 UseCase 에서")
    void serviceDoesNotUseOtherService() {
        ArchRule rule = noClasses().that().resideInAPackage("..domains..service..").and().haveSimpleNameEndingWith("Service")
                .should().dependOnClassesThat(com.tngtech.archunit.base.DescribedPredicate.describe(
                        "다른 Service", target -> target.getSimpleName().endsWith("Service") && target.getPackageName().contains(".domains.")));
        rule.check(classes);
    }

    @Test
    @DisplayName("Service 안에 record · 중첩 클래스를 두지 않는다")
    void serviceHasNoNestedClass() {
        ArchRule rule = noClasses().that().areMemberClasses().and().resideInAPackage("..domains..service..")
                .should(com.tngtech.archunit.lang.conditions.ArchConditions.be(com.tngtech.archunit.base.DescribedPredicate.describe(
                        "Service 의 중첩 클래스", c -> c.getEnclosingClass().map(e -> e.getSimpleName().endsWith("Service")).orElse(false))));
        rule.check(classes);
    }

    @Test
    @DisplayName("UseCase 에 private 메서드를 두지 않는다")
    void useCaseHasNoPrivateMethod() {
        ArchRule rule = methods().that().areDeclaredInClassesThat().areAnnotatedWith(UseCase.class)
                .and().doNotHaveModifier(JavaModifier.SYNTHETIC)
                .should().notBePrivate();
        rule.check(classes);
    }

    @Test
    @DisplayName("서비스 · 어댑터 · 유스케이스 · 엔티티는 JDK 예외를 직접 던지지 않는다")
    void noJdkExceptions() {
        ArchRule rule = noClasses().that().resideInAnyPackage("..domains..service..", "..domains..adaptor..", "..domains..domain..", "..usecase..")
                .should().callConstructor(IllegalArgumentException.class)
                .orShould().callConstructor(IllegalArgumentException.class, String.class)
                .orShould().callConstructor(IllegalArgumentException.class, Throwable.class)
                .orShould().callConstructor(IllegalArgumentException.class, String.class, Throwable.class)
                .orShould().callConstructor(IllegalStateException.class)
                .orShould().callConstructor(IllegalStateException.class, String.class)
                .orShould().callConstructor(IllegalStateException.class, Throwable.class)
                .orShould().callConstructor(IllegalStateException.class, String.class, Throwable.class);
        rule.check(classes);
    }

    @Test
    @DisplayName("UseCase 에 @Transactional 을 달지 않는다. 트랜잭션은 Service 에서")
    void useCaseIsNotTransactional() {
        ArchRule classRule = noClasses().that().areAnnotatedWith(UseCase.class)
                .should().beAnnotatedWith(Transactional.class);
        ArchRule methodRule = noMethods().that().areDeclaredInClassesThat().areAnnotatedWith(UseCase.class)
                .should().beAnnotatedWith(Transactional.class);
        classRule.check(classes);
        methodRule.check(classes);
    }

    @Test
    @DisplayName("스케줄러 · @Async 메서드에 @Transactional 을 달지 않는다. 한 번에 긴 트랜잭션이 잡히지 않게 작업 단위로 나눈다")
    void scheduledAndAsyncAreNotTransactional() {
        ArchRule rule = noMethods().that().areAnnotatedWith(Scheduled.class).or().areAnnotatedWith(Async.class)
                .should().beAnnotatedWith(Transactional.class);
        rule.check(classes);
    }

    @Test
    @DisplayName("private 메서드에 @Transactional 을 달지 않는다. 프록시를 타지 않아 적용되지 않는다")
    void privateMethodIsNotTransactional() {
        ArchRule rule = noMethods().that().arePrivate()
                .should().beAnnotatedWith(Transactional.class);
        rule.check(classes);
    }
}
