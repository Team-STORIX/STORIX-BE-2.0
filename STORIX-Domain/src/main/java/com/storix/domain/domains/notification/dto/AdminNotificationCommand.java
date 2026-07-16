package com.storix.domain.domains.notification.dto;

import com.storix.domain.domains.notification.domain.AdminNotificationType;
import com.storix.domain.domains.notification.domain.AdminNotificationSendType;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetAudience;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetType;

import java.time.LocalDateTime;

public record AdminNotificationCommand(
        String title,
        String content,
        AdminNotificationType notificationType,
        AdminNotificationTargetAudience targetAudience,
        AdminNotificationSendType sendType,
        LocalDateTime scheduledAt,
        AdminNotificationTargetType targetType,
        Long eventTargetId,
        String targetLink
) {
}
