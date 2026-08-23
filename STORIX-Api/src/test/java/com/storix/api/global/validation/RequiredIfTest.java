package com.storix.api.global.validation;

import com.storix.api.domain.plus.controller.dto.ReaderReviewUploadRequest;
import com.storix.domain.domains.plus.domain.Rating;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[검증] 다른 필드 값에 따라 필수가 되는 규칙")
class RequiredIfTest {


    private final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();

    private BindingResult validate(Object target) {
        validator.afterPropertiesSet();
        BindingResult result = new BeanPropertyBindingResult(target, "request");
        validator.validate(target, result);
        return result;
    }

    @Test
    @DisplayName("조건이 맞으면 지정한 필드로 오류가 잡힌다")
    void 조건이_맞으면_해당_필드로_잡힌다() {
        BindingResult result = validate(
                new ReaderReviewUploadRequest(1L, Rating.THREE, true, "  ", "리뷰 내용"));

        assertThat(result.getFieldErrors())
                .extracting(FieldError::getField)
                .contains("spoilerScript");

        // reason 은 어노테이션 이름 그대로 나간다. 스웨거 예시도 같은 값을 쓰므로 문서와 응답이 어긋나지 않는다
        assertThat(result.getFieldErrors())
                .extracting(FieldError::getCode)
                .contains("RequiredIf");
    }

    @Test
    @DisplayName("조건이 안 맞으면 통과한다")
    void 조건이_안맞으면_통과한다() {
        BindingResult result = validate(
                new ReaderReviewUploadRequest(1L, Rating.THREE, false, null, "리뷰 내용"));

        assertThat(result.getFieldErrors())
                .extracting(FieldError::getField)
                .doesNotContain("spoilerScript");
    }

    @Test
    @DisplayName("두 필드 순서가 뒤집히면 뒤쪽 필드로 오류가 잡힌다")
    void 순서가_뒤집히면_잡힌다() {
        java.time.LocalDateTime start = java.time.LocalDateTime.of(2026, 8, 2, 0, 0);
        java.time.LocalDateTime end = java.time.LocalDateTime.of(2026, 8, 1, 0, 0);

        BindingResult result = validate(new com.storix.api.domain.event.controller.dto.BannerRequest(
                1L, com.storix.domain.domains.event.domain.ContentTargetType.APP_EVENT,
                "배너 제목", start, end));

        assertThat(result.getFieldErrors())
                .extracting(FieldError::getField, FieldError::getCode)
                .contains(org.assertj.core.groups.Tuple.tuple("displayEndAt", "FieldsCompare"));
    }

    @Test
    @DisplayName("조건이 맞고 값도 있으면 통과한다")
    void 값이_있으면_통과한다() {
        BindingResult result = validate(
                new ReaderReviewUploadRequest(1L, Rating.THREE, true, "결말 언급", "리뷰 내용"));

        assertThat(result.getFieldErrors())
                .extracting(FieldError::getField)
                .doesNotContain("spoilerScript");
    }
}
