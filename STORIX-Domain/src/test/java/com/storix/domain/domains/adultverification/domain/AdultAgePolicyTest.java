package com.storix.domain.domains.adultverification.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[성인 판정] 청소년보호법 연도 기준")
class AdultAgePolicyTest {

    @ParameterizedTest(name = "{0} 생 / {1} 기준 → 성인={2}")
    @DisplayName("연도 차이가 19 이상이면 성인. 월·일은 보지 않는다")
    @CsvSource({
            // 2026년 기준 2007년생은 생일과 무관하게 전원 성인
            "2007-01-01, 2026-06-15, true",
            "2007-12-31, 2026-06-15, true",
            "2007-12-31, 2026-01-01, true",
            // 2008년생은 2026년에 18살 차이라 미성년
            "2008-01-01, 2026-12-31, false",
    })
    void adultIsDecidedByYearDifference(LocalDate birthDate, LocalDate baseDate, boolean expected) {
        assertThat(AdultAgePolicy.isAdult(birthDate, baseDate)).isEqualTo(expected);
    }

    @Test
    @DisplayName("연말·연초 경계 — 1월 1일이 되는 순간 성인이 된다")
    void newYearBoundary() {
        LocalDate birthDate = LocalDate.of(2008, 6, 15);

        assertThat(AdultAgePolicy.isAdult(birthDate, LocalDate.of(2026, 12, 31))).isFalse();
        assertThat(AdultAgePolicy.isAdult(birthDate, LocalDate.of(2027, 1, 1))).isTrue();
    }

    @Test
    @DisplayName("만 나이가 아니다 — 생일 전이어도 그 해 1월 1일부터 성인")
    void notKoreanCountingByBirthday() {
        // 2007-12-25 생은 2026-01-01 에 만 18세지만 성인으로 본다
        assertThat(AdultAgePolicy.isAdult(LocalDate.of(2007, 12, 25), LocalDate.of(2026, 1, 1))).isTrue();
    }

    @Test
    @DisplayName("2월 29일생도 다른 그 해 출생자와 똑같이 판정된다")
    void leapDayBirthIsNotSpecial() {
        LocalDate leapDay = LocalDate.of(2008, 2, 29);

        assertThat(AdultAgePolicy.isAdult(leapDay, LocalDate.of(2026, 12, 31))).isFalse();
        assertThat(AdultAgePolicy.isAdult(leapDay, LocalDate.of(2027, 1, 1))).isTrue();
    }

    @Test
    @DisplayName("정확히 경계값 — 19살 차이는 성인, 18살 차이는 아니다")
    void exactBoundary() {
        LocalDate baseDate = LocalDate.of(2026, 5, 1);

        assertThat(AdultAgePolicy.isAdult(LocalDate.of(2007, 5, 1), baseDate)).isTrue();
        assertThat(AdultAgePolicy.isAdult(LocalDate.of(2008, 5, 1), baseDate)).isFalse();
    }

    @Test
    @DisplayName("생년월일이 없으면 성인이 아니다")
    void nullBirthDateIsNotAdult() {
        assertThat(AdultAgePolicy.isAdult(null, LocalDate.of(2026, 5, 1))).isFalse();
        assertThat(AdultAgePolicy.isAdult(LocalDate.of(2000, 1, 1), null)).isFalse();
    }
}
