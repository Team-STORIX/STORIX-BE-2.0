package com.storix.api.global.validation;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;

/**
 * @RequiredIf · @FieldsCompare 는 필드를 문자열로 가리켜서 오타가 컴파일에 안 잡힌다.
 * 기동할 때 한 번 훑어서 틀린 이름이 있으면 아예 뜨지 않게 한다.
 */
@Slf4j
@Component
public class ValidationRuleVerifier {

    private static final String SCAN_BASE = "com.storix.api";

    @PostConstruct
    void verify() {
        List<String> broken = findBroken(SCAN_BASE);
        if (!broken.isEmpty()) {
            throw new IllegalStateException("검증 어노테이션이 없는 필드를 가리킵니다: " + String.join(", ", broken));
        }
        log.info("검증 규칙 확인 완료 targets={}", annotatedTypes(SCAN_BASE).size());
    }

    public static List<String> findBroken(String scanBase) {
        List<String> broken = new ArrayList<>();
        for (Class<?> type : annotatedTypes(scanBase)) {
            for (RequiredIf rule : type.getAnnotationsByType(RequiredIf.class)) {
                check(type, rule.when(), broken);
                check(type, rule.field(), broken);
            }
            for (FieldsCompare rule : type.getAnnotationsByType(FieldsCompare.class)) {
                check(type, rule.before(), broken);
                check(type, rule.after(), broken);
                check(type, rule.field(), broken);
            }
        }
        return broken;
    }

    public static List<Class<?>> annotatedTypes(String scanBase) {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);
        for (Class<? extends Annotation> annotation :
                List.of(RequiredIf.class, RequiredIf.List.class, FieldsCompare.class, FieldsCompare.List.class)) {
            scanner.addIncludeFilter(new AnnotationTypeFilter(annotation));
        }

        List<Class<?>> types = new ArrayList<>();
        for (BeanDefinition definition : scanner.findCandidateComponents(scanBase)) {
            try {
                types.add(Class.forName(definition.getBeanClassName()));
            } catch (ClassNotFoundException e) {
                throw new IllegalStateException("검증 대상 클래스를 읽지 못했습니다: " + definition.getBeanClassName(), e);
            }
        }
        return types;
    }

    private static void check(Class<?> type, String name, List<String> broken) {
        if (FieldAccess.accessor(type, name).isEmpty()) {
            broken.add(type.getSimpleName() + "." + name);
        }
    }
}
