package com.storix.domain.domains.works.domain;

import com.storix.domain.domains.adultverification.exception.NotAdultVerifiedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[성인 콘텐츠 게이팅] 연령등급 + 인증 유효성")
class AdultContentPolicyTest {

    private static final LocalDateTime VALID = LocalDateTime.now().minusMonths(1);
    private static final LocalDateTime EXPIRED = LocalDateTime.now().minusYears(2);

    @ParameterizedTest(name = "{0}")
    @DisplayName("18세 이용가만 성인 작품이다")
    @EnumSource(AgeClassification.class)
    void onlyAge18IsAdultOnly(AgeClassification ageClassification) {
        boolean expected = ageClassification == AgeClassification.AGE_18;

        assertThat(AdultContentPolicy.isAdultOnly(ageClassification)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("일반 작품은 미인증·비로그인이어도 통과한다")
    @EnumSource(value = AgeClassification.class, names = "AGE_18", mode = EnumSource.Mode.EXCLUDE)
    void nonAdultWorksAlwaysPass(AgeClassification ageClassification) {
        assertThatCode(() -> AdultContentPolicy.check(ageClassification, () -> null))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("일반 작품에서는 인증 상태를 조회하지 않는다")
    void nonAdultWorksDoNotLoadVerification() {
        AtomicInteger loadCount = new AtomicInteger();

        AdultContentPolicy.check(AgeClassification.ALL, () -> {
            loadCount.incrementAndGet();
            return VALID;
        });

        assertThat(loadCount.get()).isZero();
    }

    @Test
    @DisplayName("성인 작품 + 유효한 인증 → 통과")
    void adultWorksWithValidVerificationPass() {
        assertThatCode(() -> AdultContentPolicy.check(AgeClassification.AGE_18, () -> VALID))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("성인 작품 + 미인증 → NotAdultVerifiedException")
    void adultWorksWithoutVerificationThrow() {
        assertThatThrownBy(() -> AdultContentPolicy.check(AgeClassification.AGE_18, () -> null))
                .isInstanceOf(NotAdultVerifiedException.class);
    }

    @Test
    @DisplayName("성인 작품 + 만료된 인증 → NotAdultVerifiedException")
    void adultWorksWithExpiredVerificationThrow() {
        assertThatThrownBy(() -> AdultContentPolicy.check(AgeClassification.AGE_18, () -> EXPIRED))
                .isInstanceOf(NotAdultVerifiedException.class);
    }

    @Test
    @DisplayName("boolean 오버로드도 같은 규칙을 따른다")
    void booleanOverloadBehavesSame() {
        assertThatCode(() -> AdultContentPolicy.check(false, () -> null))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> AdultContentPolicy.check(true, () -> null))
                .isInstanceOf(NotAdultVerifiedException.class);
    }
}
