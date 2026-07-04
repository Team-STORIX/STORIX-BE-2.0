package com.storix.api.domain.notification.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.storix.domain.domains.notification.domain.AdminNotificationSendType;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetAudience;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetType;
import com.storix.domain.domains.notification.dto.AdminNotificationCommand;
import com.storix.domain.domains.notification.domain.AdminNotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public record AdminNotificationRequest(
        @Schema(description = "운영자 알림 제목", example = "여름맞이 이벤트 안내")
        @NotBlank(message = "제목은 필수입니다.")
        @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
        String title,

        @Schema(description = "운영자 알림 내용", example = "지금 앱에서 여름맞이 이벤트를 확인하세요!")
        @NotBlank(message = "내용은 필수입니다.")
        @Size(max = 500, message = "내용은 500자 이하여야 합니다.")
        String content,

        @Schema(description = "알림 타입 (MARKETING: 마케팅/광고, FEATURE_UPDATE: 신기능, TOS_UPDATE: 약관, PRIVACY_UPDATE: 개인정보 처리방침). 마케팅만 수신 동의자에게 발송되고 (광고)/(수신거부) 표기가 붙습니다.", example = "MARKETING")
        @NotNull(message = "알림 타입은 필수입니다.")
        AdminNotificationType notificationType,

        @Schema(description = "발송 대상 (ALL: 전체 유저, NEW_USERS: 가입 첫 달 신규 유저)", example = "ALL")
        @NotNull(message = "발송 대상은 필수입니다.")
        AdminNotificationTargetAudience targetAudience,

        @Schema(description = "발송 방식 (IMMEDIATE: 즉시 발송, SCHEDULED: 예약 발송)", example = "SCHEDULED")
        @NotNull(message = "발송 방식은 필수입니다.")
        AdminNotificationSendType sendType,

        @Schema(description = "예약 발송 시각 (예약 발송 시에만, 미래). 형식: yyyy-MM-dd HH:mm (KST)",
                example = "2026-07-04 14:00", type = "string")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm", timezone = "Asia/Seoul")
        LocalDateTime scheduledAt,

        @Schema(description = "알림 탭 시 이동 타겟 (NONE: 없음, APP_EVENT: 자사 이벤트 상세, EXTERNAL: 외부 URL). 미지정 시 NONE", example = "NONE")
        AdminNotificationTargetType targetType,

        @Schema(description = "APP_EVENT 일 때 이동할 자사 이벤트 id", example = "10")
        Long eventTargetId,

        @Schema(description = "EXTERNAL 일 때 이동할 앱 외부 URL", example = "https://forms.gle/xxxx")
        @Size(max = 1000, message = "링크는 1000자 이하여야 합니다.")
        String targetLink
) {
    @AssertTrue(message = "즉시 발송 시에는 예약 발송 시각을 지정할 수 없습니다.")
    private boolean isScheduledAtAbsentWhenImmediate() {
        return sendType != AdminNotificationSendType.IMMEDIATE || scheduledAt == null;
    }

    @AssertTrue(message = "예약 발송 시 예약 발송 시각은 필수입니다.")
    private boolean isScheduledAtPresentWhenScheduled() {
        return sendType != AdminNotificationSendType.SCHEDULED || scheduledAt != null;
    }

    @AssertTrue(message = "예약 발송 시각은 미래여야 합니다.")
    private boolean isScheduledAtFutureWhenScheduled() {
        return sendType != AdminNotificationSendType.SCHEDULED
                || scheduledAt == null
                || scheduledAt.isAfter(LocalDateTime.now());
    }

    @AssertTrue(message = "타겟 타입에 필요한 값이 없습니다. (APP_EVENT: eventTargetId, EXTERNAL: targetLink)")
    private boolean isTargetConsistent() {
        if (targetType == null) return true;
        return switch (targetType) {
            case APP_EVENT -> eventTargetId != null;
            case EXTERNAL -> targetLink != null && !targetLink.isBlank();
            case NONE -> true;
        };
    }

    @AssertTrue(message = "외부 링크는 https:// 로 시작하는 URL이어야 합니다.")
    private boolean isTargetLinkSafe() {
        if (targetLink == null || targetLink.isBlank()) return true;
        String normalized = targetLink.trim().toLowerCase();
        return normalized.startsWith("https://");
    }

    public AdminNotificationCommand toCommand() {
        return new AdminNotificationCommand(
                title, content, notificationType, targetAudience, sendType, scheduledAt,
                targetType, eventTargetId, targetLink);
    }
}
