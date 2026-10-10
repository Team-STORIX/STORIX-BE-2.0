package com.storix.batch.architecture;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.transaction.annotation.Transactional;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noMethods;

@DisplayName("[컨벤션] 배치 트랜잭션 규칙")
class BatchTransactionArchitectureTest {

    private static final JavaClasses CLASSES = new ClassFileImporter()
            .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
            .importPackages("com.storix.batch");

    @Test
    @DisplayName("스케줄러 · @Async 메서드에 @Transactional 을 달지 않는다. 트랜잭션은 도메인 Service · Adaptor 에서")
    void scheduledAndAsyncAreNotTransactional() {
        ArchRule rule = noMethods().that().areAnnotatedWith(Scheduled.class).or().areAnnotatedWith(Async.class)
                .should().beAnnotatedWith(Transactional.class);
        rule.check(CLASSES);
    }

    @Test
    @DisplayName("배치 클래스에 @Transactional 을 달지 않는다")
    void batchClassIsNotTransactional() {
        ArchRule rule = noClasses().should().beAnnotatedWith(Transactional.class);
        rule.check(CLASSES);
    }
}
