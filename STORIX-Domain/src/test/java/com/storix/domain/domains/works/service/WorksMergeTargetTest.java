package com.storix.domain.domains.works.service;

import com.storix.domain.domains.works.domain.Works;
import com.storix.domain.domains.works.domain.WorksMergeTarget;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[작품] 병합 대상 테이블")
class WorksMergeTargetTest {

    @Test
    @DisplayName("works_id 를 가진 엔티티는 모두 WorksMergeTarget 에 있다. 새 엔티티가 생기면 enum 과 WorksMergeService 에 옮기는 처리를 추가해야 한다")
    void allWorksReferencesAreMerged() {
        ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        Set<Class<?>> referencing = new HashSet<>();
        for (BeanDefinition definition : scanner.findCandidateComponents("com.storix.domain")) {
            Class<?> entity = ClassUtils.resolveClassName(definition.getBeanClassName(), getClass().getClassLoader());
            if (entity == Works.class) continue;
            if (fieldsOf(entity).anyMatch(WorksMergeTargetTest::referencesWorks)) referencing.add(entity);
        }

        assertThat(referencing).containsExactlyInAnyOrderElementsOf(
                Arrays.stream(WorksMergeTarget.values()).map(WorksMergeTarget::getEntity).toList());
    }

    private static Stream<Field> fieldsOf(Class<?> type) {
        Stream<Field> own = Arrays.stream(type.getDeclaredFields());
        return type.getSuperclass() == null ? own : Stream.concat(own, fieldsOf(type.getSuperclass()));
    }

    private static boolean referencesWorks(Field field) {
        Column column = field.getAnnotation(Column.class);
        JoinColumn joinColumn = field.getAnnotation(JoinColumn.class);
        return (column != null && column.name().equals("works_id"))
                || (joinColumn != null && joinColumn.name().equals("works_id"))
                || field.getType() == Works.class;
    }

    @Test
    @DisplayName("병합 대상마다 @Table 이름이 있어 응답의 테이블 이름으로 쓸 수 있다")
    void everyTargetHasTableName() {
        assertThat(WorksMergeTarget.values()).allSatisfy(target -> assertThat(target.tableName()).isNotBlank());
    }
}
