package com.storix.domain.domains.adultverification.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[성인인증 유효기간] 인증일로부터 1년, 날짜 기준")
class AdultVerificationPolicyTest {

    @Test
    @DisplayName("만료일은 인증한 날의 1년 뒤 같은 날짜")
    void expiresOnSameDateNextYear() {
        LocalDateTime verifiedAt = LocalDateTime.of(2026, 8, 21, 14, 30);

        assertThat(AdultVerificationPolicy.expiresOn(verifiedAt)).isEqualTo(LocalDate.of(2027, 8, 21));
    }

    @Test
    @DisplayName("인증 시각은 만료일에 영향을 주지 않는다")
    void timeOfDayDoesNotMatter() {
        LocalDate expected = LocalDate.of(2027, 8, 21);

        assertThat(AdultVerificationPolicy.expiresOn(LocalDateTime.of(2026, 8, 21, 0, 0))).isEqualTo(expected);
        assertThat(AdultVerificationPolicy.expiresOn(LocalDateTime.of(2026, 8, 21, 23, 59, 59))).isEqualTo(expected);
    }

    @Test
    @DisplayName("윤년 2월 29일 인증은 다음 해 2월 28일로 당겨진다")
    void leapDayExpiryIsPulledBack() {
        LocalDateTime leapDay = LocalDateTime.of(2028, 2, 29, 9, 0);

        assertThat(AdultVerificationPolicy.expiresOn(leapDay)).isEqualTo(LocalDate.of(2029, 2, 28));
    }

    @Test
    @DisplayName("평년 2월 28일 인증은 다음 해 2월 28일 그대로")
    void nonLeapDayExpiryIsUnchanged() {
        LocalDateTime day = LocalDateTime.of(2027, 2, 28, 9, 0);

        assertThat(AdultVerificationPolicy.expiresOn(day)).isEqualTo(LocalDate.of(2028, 2, 28));
    }

    @Test
    @DisplayName("만료일 당일까지 유효하고 다음 날부터 만료")
    void validUntilExpiryDateInclusive() {
        LocalDateTime verifiedAt = LocalDateTime.of(2026, 8, 21, 14, 30);

        assertThat(AdultVerificationPolicy.isValidOn(verifiedAt, LocalDate.of(2027, 8, 20))).isTrue();
        assertThat(AdultVerificationPolicy.isValidOn(verifiedAt, LocalDate.of(2027, 8, 21))).isTrue();
        assertThat(AdultVerificationPolicy.isValidOn(verifiedAt, LocalDate.of(2027, 8, 22))).isFalse();
    }

    @Test
    @DisplayName("인증 당일도 유효하다")
    void validOnVerificationDay() {
        LocalDateTime verifiedAt = LocalDateTime.of(2026, 8, 21, 14, 30);

        assertThat(AdultVerificationPolicy.isValidOn(verifiedAt, LocalDate.of(2026, 8, 21))).isTrue();
    }

    @Test
    @DisplayName("윤년생 만료 경계 — 2029년 2월 28일까지 유효, 3월 1일부터 만료")
    void leapDayExpiryBoundary() {
        LocalDateTime leapDay = LocalDateTime.of(2028, 2, 29, 9, 0);

        assertThat(AdultVerificationPolicy.isValidOn(leapDay, LocalDate.of(2029, 2, 28))).isTrue();
        assertThat(AdultVerificationPolicy.isValidOn(leapDay, LocalDate.of(2029, 3, 1))).isFalse();
    }

    @Test
    @DisplayName("인증 이력이 없으면 유효하지 않다")
    void nullVerifiedAtIsNotValid() {
        assertThat(AdultVerificationPolicy.isValidOn(null, LocalDate.of(2026, 8, 21))).isFalse();
    }
}
