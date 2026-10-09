package com.storix.domain.domains.works.domain;

import com.storix.domain.domains.adultverification.exception.NotAdultVerifiedException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[성인 콘텐츠 게이팅] 연령등급 + 열람 가능 여부")
class AdultContentPolicyTest {

    @ParameterizedTest(name = "{0}")
    @DisplayName("18세 이용가만 성인 작품이다")
    @EnumSource(AgeClassification.class)
    void onlyAge18IsAdultOnly(AgeClassification ageClassification) {
        boolean expected = ageClassification == AgeClassification.AGE_18;
        assertThat(AdultContentPolicy.isAdultOnly(ageClassification)).isEqualTo(expected);
    }

    @ParameterizedTest(name = "{0}")
    @DisplayName("일반 작품은 열람 권한이 없어도 통과한다")
    @EnumSource(value = AgeClassification.class, names = "AGE_18", mode = EnumSource.Mode.EXCLUDE)
    void nonAdultWorksAlwaysPass(AgeClassification ageClassification) {
        assertThatCode(() -> AdultContentPolicy.check(ageClassification, () -> false))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("일반 작품에서는 열람 권한을 조회하지 않는다")
    void nonAdultWorksDoNotLoadPermission() {
        AtomicInteger loadCount = new AtomicInteger();
        AdultContentPolicy.check(AgeClassification.ALL, () -> {
            loadCount.incrementAndGet();
            return true;
        });

        assertThat(loadCount.get()).isZero();
    }

    @Test
    @DisplayName("성인 작품 + 열람 가능 → 통과")
    void adultWorksWithPermissionPass() {
        assertThatCode(() -> AdultContentPolicy.check(AgeClassification.AGE_18, () -> true))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("성인 작품 + 열람 불가 → NotAdultVerifiedException")
    void adultWorksWithoutPermissionThrow() {
        assertThatThrownBy(() -> AdultContentPolicy.check(AgeClassification.AGE_18, () -> false))
                .isInstanceOf(NotAdultVerifiedException.class);
    }

    @Test
    @DisplayName("boolean 오버로드도 같은 규칙을 따른다")
    void booleanOverloadBehavesSame() {
        assertThatCode(() -> AdultContentPolicy.check(false, () -> false))
                .doesNotThrowAnyException();
        assertThatThrownBy(() -> AdultContentPolicy.check(true, () -> false))
                .isInstanceOf(NotAdultVerifiedException.class);
    }
}
