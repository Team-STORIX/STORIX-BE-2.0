package com.storix.api.global.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 두 필드의 순서를 검증한다. before 가 after 보다 앞서야 한다.
 * 둘 중 하나라도 비어 있으면 통과시킨다 (부재는 @NotNull 이 담당).
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(FieldsCompare.List.class)
@Constraint(validatedBy = FieldsCompareValidator.class)
public @interface FieldsCompare {

    String before();

    String after();

    /** 위반을 붙일 필드. 응답 fieldErrors 와 스웨거 예시가 같은 값을 쓴다. */
    String field();

    boolean allowEqual() default false;

    String message() default "값의 순서가 올바르지 않습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Documented
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        FieldsCompare[] value();
    }
}
