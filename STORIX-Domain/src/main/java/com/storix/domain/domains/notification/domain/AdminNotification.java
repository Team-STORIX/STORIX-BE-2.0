package com.storix.domain.domains.notification.domain;

import com.storix.common.model.BaseTimeEntity;
import com.storix.common.utils.NightWindow;
import com.storix.domain.domains.notification.exception.AdminNotificationMarketingNightException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(
        name = "admin_notifications",
        indexes = {
                @Index(name = "idx_admin_notification_status_scheduled", columnList = "status, scheduled_at")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminNotification extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_notification_id")
    private Long id;

    // 제목
    @Column(nullable = false, length = 100)
    private String title;

    // 내용
    @Column(nullable = false, length = 500)
    private String content;

    // 운영자 발송 타입 (MARKETING / FEATURE_UPDATE / TOS_UPDATE / PRIVACY_UPDATE)
    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false, length = 30)
    private AdminNotificationType notificationType;

    // 발송 대상
    @Enumerated(EnumType.STRING)
    @Column(name = "target_audience", nullable = false, length = 20)
    private AdminNotificationTargetAudience targetAudience;

    // 발송 방식
    @Enumerated(EnumType.STRING)
    @Column(name = "send_type", nullable = false, length = 20)
    private AdminNotificationSendType sendType;

    // 예약 일시
    @Column(name = "scheduled_at")
    private LocalDateTime scheduledAt;

    // 알림 탭 시 이동 타겟 (NONE / APP_EVENT: eventTargetId / EXTERNAL: link)
    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private AdminNotificationTargetType targetType;

    // targetType = APP_EVENT 일 때 이동할 자사 이벤트 id
    @Column(name = "event_target_id")
    private Long eventTargetId;

    // targetType = EXTERNAL 일 때 이동할 앱 외부 URL
    @Column(name = "link", length = 1000)
    private String targetLink;

    // 관리자 id
    @Column(name = "assignee_admin_id")
    private Long assigneeAdminId;

    // 발송 상태
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AdminNotificationStatus status;

    // 발송 통계 - 대상 수 / 성공 수 / 실패 수 / 발송 대상외 수
    @Column(name = "target_count", nullable = false)
    private int targetCount;

    @Column(name = "sent_count", nullable = false)
    private int sentCount;

    @Column(name = "failed_count", nullable = false)
    private int failedCount;

    @Column(name = "skipped_count", nullable = false)
    private int skippedCount;

    // 모든 청크 발행 완료 여부
    @Column(name = "is_all_chunk_published", nullable = false)
    private boolean isAllChunkPublished;

    // 마지막 청크 발행 유저 id
    @Column(name = "last_broadcast_user_id")
    private Long lastBroadcastUserId;


    @Builder
    public AdminNotification(String title,
                             String content,
                             AdminNotificationType notificationType,
                             AdminNotificationTargetAudience targetAudience,
                             AdminNotificationSendType sendType,
                             LocalDateTime scheduledAt,
                             AdminNotificationTargetType targetType,
                             Long eventTargetId,
                             String targetLink,
                             Long assigneeAdminId) {
        this.title = title;
        this.content = content;
        this.notificationType = notificationType;
        this.targetAudience = targetAudience;
        this.sendType = sendType;
        this.scheduledAt = sendType == AdminNotificationSendType.IMMEDIATE ? LocalDateTime.now() : scheduledAt;
        this.targetType = targetType != null ? targetType : AdminNotificationTargetType.NONE;
        this.eventTargetId = eventTargetId;
        this.targetLink = targetLink;
        this.assigneeAdminId = assigneeAdminId;
        this.status = AdminNotificationStatus.SCHEDULED;
        validateMarketingSendTime();
    }

    public void update(String title,
                       String content,
                       AdminNotificationType notificationType,
                       AdminNotificationTargetAudience targetAudience,
                       AdminNotificationSendType sendType,
                       LocalDateTime scheduledAt,
                       AdminNotificationTargetType targetType,
                       Long eventTargetId,
                       String targetLink) {
        this.title = title;
        this.content = content;
        this.notificationType = notificationType;
        this.targetAudience = targetAudience;
        this.sendType = sendType;
        this.scheduledAt = sendType == AdminNotificationSendType.IMMEDIATE ? LocalDateTime.now() : scheduledAt;
        this.targetType = targetType != null ? targetType : AdminNotificationTargetType.NONE;
        this.eventTargetId = eventTargetId;
        this.targetLink = targetLink;
        validateMarketingSendTime();
    }

    // 마케팅은 수신 동의 없이 야간(21시~익일 8시) 발송 불가
    private void validateMarketingSendTime() {
        if (notificationType == AdminNotificationType.MARKETING
                && scheduledAt != null && NightWindow.isNight(scheduledAt)) {
            throw AdminNotificationMarketingNightException.EXCEPTION;
        }
    }

    public void cancel() {
        this.status = AdminNotificationStatus.CANCELED;
    }

    public boolean isScheduled() {
        return status == AdminNotificationStatus.SCHEDULED;
    }

    // 수동 재발송 가능 여부
    public boolean isRebroadcastable() {
        return status == AdminNotificationStatus.FAILED;
    }
}
