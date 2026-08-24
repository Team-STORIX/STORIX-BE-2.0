package com.storix.infrastructure.external.portone.dto;

import com.storix.domain.domains.adultverification.domain.IdentityVerificationStatus;
import com.storix.domain.domains.adultverification.dto.IdentityVerificationResult;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[포트원 응답] 인증 시각 변환")
class PortOneIdentityVerificationResponseTest {

    // 운영은 기동 시 기본 시간대를 고정한다. 컨텍스트를 안 띄우는 테스트에서는 직접 맞춰준다
    private static TimeZone origin;

    @BeforeAll
    static void fixTimeZone() {
        origin = TimeZone.getDefault();
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"));
    }

    @AfterAll
    static void restoreTimeZone() {
        TimeZone.setDefault(origin);
    }

    private PortOneIdentityVerificationResponse response(OffsetDateTime verifiedAt) {
        return new PortOneIdentityVerificationResponse(
                "VERIFIED", "identity-verification-1", verifiedAt, "pg-tx-1",
                new PortOneVerifiedCustomer(LocalDate.of(2000, 3, 5)), null);
    }

    @Test
    @DisplayName("UTC 로 와도 KST 로 옮긴다")
    void convertsUtcToSeoul() {
        IdentityVerificationResult result = response(OffsetDateTime.parse("2026-08-24T08:16:09Z")).toResult();

        assertThat(result.verifiedAt()).isEqualTo(LocalDateTime.of(2026, 8, 24, 17, 16, 9));
    }

    @Test
    @DisplayName("KST 오프셋으로 와도 같은 시각이 된다")
    void convertsSeoulOffsetToSameTime() {
        IdentityVerificationResult result = response(OffsetDateTime.parse("2026-08-24T17:16:09+09:00")).toResult();

        assertThat(result.verifiedAt()).isEqualTo(LocalDateTime.of(2026, 8, 24, 17, 16, 9));
    }

    @Test
    @DisplayName("자정을 넘기는 오프셋도 날짜까지 맞춘다")
    void convertsAcrossDateBoundary() {
        IdentityVerificationResult result = response(OffsetDateTime.parse("2026-08-23T16:30:00Z")).toResult();

        assertThat(result.verifiedAt()).isEqualTo(LocalDateTime.of(2026, 8, 24, 1, 30, 0));
    }

    @Test
    @DisplayName("verifiedAt 이 없으면 null")
    void nullVerifiedAt() {
        assertThat(response(null).toResult().verifiedAt()).isNull();
    }

    @Test
    @DisplayName("status 는 그대로 옮긴다")
    void mapsStatus() {
        assertThat(response(null).toResult().status()).isEqualTo(IdentityVerificationStatus.VERIFIED);
    }
}
