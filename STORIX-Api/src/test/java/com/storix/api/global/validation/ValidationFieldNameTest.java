package com.storix.api.global.validation;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/** 기동 시 검사와 같은 로직을 CI 에서도 돌려, 틀린 이름을 커밋 단계에서 잡는다. */
@DisplayName("[검증] 어노테이션이 가리키는 필드 이름이 실재하는가")
class ValidationFieldNameTest {

    private static final String SCAN_BASE = "com.storix.api";

    @Test
    void 모든_검증_어노테이션의_필드명이_실재한다() {
        assertThat(ValidationRuleVerifier.findBroken(SCAN_BASE))
                .as("접근자를 찾지 못한 필드 이름")
                .isEmpty();
    }

    @Test
    @DisplayName("기동 시 검사가 통과한다 (실패하면 앱이 뜨지 않는다)")
    void 기동_검사가_통과한다() {
        assertThatCode(() -> new ValidationRuleVerifier().verify()).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("검사 대상이 실제로 잡히는지 (빈 목록이면 위 테스트가 무의미해진다)")
    void 검사_대상이_비어있지_않다() {
        assertThat(ValidationRuleVerifier.annotatedTypes(SCAN_BASE)).isNotEmpty();
    }
}
