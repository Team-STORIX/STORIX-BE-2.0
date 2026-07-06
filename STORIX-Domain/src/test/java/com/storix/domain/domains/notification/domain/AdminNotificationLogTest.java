package com.storix.domain.domains.notification.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("[운영자 알림 로그] 상태 변경 / 지연 재시도 백오프")
class AdminNotificationLogTest {

    private static final int MAX_ATTEMPTS = 3;
    private static final LocalDateTime NOW = LocalDateTime.of(2026, 1, 1, 0, 0);

    private AdminNotificationLog newLog() {
        return AdminNotificationLog.builder()
                .adminNotificationId(1L)
                .userId(10L)
                .notificationId(100L)
                .build();
    }

    @Test
    @DisplayName("신규 로그는 PENDING, attempts=0, nextRetryAt=null")
    void newLogIsPending() {
        AdminNotificationLog log = newLog();
        assertThat(log.getStatus()).isEqualTo(AdminNotificationLogStatus.PENDING);
        assertThat(log.getAttempts()).isZero();
        assertThat(log.getNextRetryAt()).isNull();
    }

    @Test
    @DisplayName("일시 실패 -> 상한 전까지 PENDING 유지 + 지수 백오프(5,10분)로 nextRetryAt 설정")
    void transientFailureSetsBackoff() {
        AdminNotificationLog log = newLog();

        log.recordTransientFailure(MAX_ATTEMPTS, NOW);
        assertThat(log.getStatus()).isEqualTo(AdminNotificationLogStatus.PENDING);
        assertThat(log.getAttempts()).isEqualTo(1);
        assertThat(log.getNextRetryAt()).isEqualTo(NOW.plusMinutes(5));   // 5 * 2^0

        log.recordTransientFailure(MAX_ATTEMPTS, NOW);
        assertThat(log.getStatus()).isEqualTo(AdminNotificationLogStatus.PENDING);
        assertThat(log.getAttempts()).isEqualTo(2);
        assertThat(log.getNextRetryAt()).isEqualTo(NOW.plusMinutes(10));  // 5 * 2^1
    }

    @Test
    @DisplayName("일시 실패가 상한 도달 → FAILED 확정 + nextRetryAt 초기화")
    void transientFailureExhaustsToFailed() {
        AdminNotificationLog log = newLog();
        log.recordTransientFailure(MAX_ATTEMPTS, NOW);
        log.recordTransientFailure(MAX_ATTEMPTS, NOW);

        log.recordTransientFailure(MAX_ATTEMPTS, NOW); // 3회차 = 상한

        assertThat(log.getStatus()).isEqualTo(AdminNotificationLogStatus.FAILED);
        assertThat(log.getAttempts()).isEqualTo(3);
        assertThat(log.getNextRetryAt()).isNull();
    }

    @Test
    @DisplayName("백오프는 최대 60분으로 캡")
    void backoffCappedAt60Minutes() {
        AdminNotificationLog log = newLog();
        // 상한을 크게 두고 반복 → 5,10,20,40,(80→60),(160→60)
        for (int i = 0; i < 4; i++) {
            log.recordTransientFailure(100, NOW);
        }
        assertThat(log.getNextRetryAt()).isEqualTo(NOW.plusMinutes(40)); // attempts=4 → 5*2^3
        log.recordTransientFailure(100, NOW);
        assertThat(log.getNextRetryAt()).isEqualTo(NOW.plusMinutes(60)); // attempts=5 → 5*2^4=80 → cap 60
    }
}
