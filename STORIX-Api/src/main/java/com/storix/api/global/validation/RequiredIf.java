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
 * 다른 필드가 특정 값일 때만 필수인 필드를 선언한다.
 * 예) 유형이 APP_EVENT 면 appEventId 필수, 스포일러면 가림막 문구 필수.
 */
@Documented
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(RequiredIf.List.class)
@Constraint(validatedBy = RequiredIfValidator.class)
public @interface RequiredIf {

    String when();

    String is();

    /** 필수가 되는 필드. 응답 fieldErrors 와 스웨거 예시가 같은 값을 쓴다. */
    String field();

    String message() default "필수 값이 누락되었습니다.";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

    @Documented
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface List {
        RequiredIf[] value();
    }
}
