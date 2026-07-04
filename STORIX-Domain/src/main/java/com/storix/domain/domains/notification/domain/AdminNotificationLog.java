package com.storix.domain.domains.notification.domain;

import com.storix.common.model.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;


@Entity
@Getter
@Table(
        name = "admin_notification_log",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_admin_notification_log_user",
                columnNames = {"admin_notification_id", "user_id"}  // 멱등키
        ),
        indexes = {
                @Index(name = "idx_admin_notification_log_status", columnList = "admin_notification_id, status"),
                @Index(name = "idx_admin_notification_log_retry", columnList = "status, next_retry_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminNotificationLog extends BaseTimeEntity {

    // 지수 백오프(분): 5, 10, 20 ... 최대 60
    private static final long BASE_BACKOFF_MINUTES = 5;
    private static final long MAX_BACKOFF_MINUTES = 60;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 운영자 알림 id
    @Column(name = "admin_notification_id", nullable = false)
    private Long adminNotificationId;

    // 유저 id
    @Column(name = "user_id", nullable = false)
    private Long userId;

    // 인앱 알림 id
    @Column(name = "notification_id")
    private Long notificationId;

    // 어드민 알림 발송 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminNotificationLogStatus status;

    // 발송 시도 횟수 (재시도 상한 : 3회)
    @Column(nullable = false)
    private int attempts;

    // 발송 성공 시각
    @Column(name = "sent_at")
    private LocalDateTime sentAt;

    // 다음 재시도 가능 시각
    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;


    @Builder
    public AdminNotificationLog(Long adminNotificationId, Long userId, Long notificationId) {
        this.adminNotificationId = adminNotificationId;
        this.userId = userId;
        this.notificationId = notificationId;
        this.status = AdminNotificationLogStatus.PENDING;
        this.attempts = 0;
    }

    // 발송 전 미리 PENDING 으로 생성
    public static AdminNotificationLog pending(Long adminNotificationId, Long userId, LocalDateTime eligibleAt) {
        AdminNotificationLog log = new AdminNotificationLog(adminNotificationId, userId, null);
        log.nextRetryAt = eligibleAt;

        return log;
    }

    // 발송 시점에 생성한 인앱 알림 id 연결
    public void assignNotification(Long notificationId) {
        this.notificationId = notificationId;
    }

    // 일시 실패 -> 시도 횟수 누적
    public void recordTransientFailure(int maxAttempts, LocalDateTime now) {
        // 발송 시도 횟수 누적
        this.attempts++;

        // 1. 재시도 상한 초과 시 영구 실패
        if (this.attempts >= maxAttempts) {
            this.status = AdminNotificationLogStatus.FAILED;
            this.nextRetryAt = null;
        }
        // 2. 지수 백오프로 다음 재시도 시각 설정
        else {
            long minutes = Math.min(BASE_BACKOFF_MINUTES * (1L << (this.attempts - 1)), MAX_BACKOFF_MINUTES);
            this.nextRetryAt = now.plusMinutes(minutes);
        }
    }
}
