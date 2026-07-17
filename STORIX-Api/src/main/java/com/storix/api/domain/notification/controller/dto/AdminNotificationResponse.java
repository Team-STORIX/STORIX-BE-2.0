package com.storix.api.domain.notification.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.storix.domain.domains.notification.domain.AdminNotification;
import com.storix.domain.domains.notification.domain.AdminNotificationStatus;
import com.storix.domain.domains.notification.domain.AdminNotificationType;
import com.storix.domain.domains.notification.domain.AdminNotificationSendType;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetAudience;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record AdminNotificationResponse(
        Long id,
        String title,
        String content,

        @Schema(description = "알림 타입 (MARKETING: 마케팅/광고, FEATURE_UPDATE: 신기능, TOS_UPDATE: 약관, PRIVACY_UPDATE: 개인정보 처리방침)")
        AdminNotificationType notificationType,

        @Schema(description = "발송 대상 (ALL: 전체 유저, NEW_USERS: 가입 첫 달 신규 유저)")
        AdminNotificationTargetAudience targetAudience,

        @Schema(description = "발송 방식 (IMMEDIATE: 즉시 발송, SCHEDULED: 예약 발송)")
        AdminNotificationSendType sendType,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime scheduledAt,

        @Schema(description = "발송 상태 (SCHEDULED: 발송 예정, SENDING: 발송 중, SENT: 발송 완료, FAILED: 발송 실패, CANCELED: 취소). 부분/전체 실패는 sentCount·failedCount 로 구분")
        AdminNotificationStatus status,

        @Schema(description = "수동 재발송 가능 여부 (발송 실패일 때만 true)")
        boolean rebroadcastable,

        @Schema(description = "알림 탭 시 이동 타겟 (NONE: 없음, APP_EVENT: 자사 이벤트 상세, EXTERNAL: 외부 URL)")
        AdminNotificationTargetType targetType,

        @Schema(description = "이동할 자사 이벤트 id (targetType=APP_EVENT 일 때만, 그 외 null)")
        Long eventTargetId,

        @Schema(description = "이동할 앱 외부 URL (targetType=EXTERNAL 일 때만, 그 외 null)")
        String targetLink,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime createdAt,

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime updatedAt
) {
    public static AdminNotificationResponse from(AdminNotification adminNotification) {
        return AdminNotificationResponse.builder()
                .id(adminNotification.getId())
                .title(adminNotification.getTitle())
                .content(adminNotification.getContent())
                .notificationType(adminNotification.getNotificationType())
                .targetAudience(adminNotification.getTargetAudience())
                .sendType(adminNotification.getSendType())
                .scheduledAt(adminNotification.getScheduledAt())
                .status(adminNotification.getStatus())
                .rebroadcastable(adminNotification.isRebroadcastable())
                .targetType(adminNotification.getTargetType())
                .eventTargetId(adminNotification.getTargetType() == AdminNotificationTargetType.APP_EVENT
                        ? adminNotification.getEventTargetId() : null)
                .targetLink(adminNotification.getTargetType() == AdminNotificationTargetType.EXTERNAL
                        ? adminNotification.getTargetLink() : null)
                .createdAt(adminNotification.getCreatedAt())
                .updatedAt(adminNotification.getUpdatedAt())
                .build();
    }
}
