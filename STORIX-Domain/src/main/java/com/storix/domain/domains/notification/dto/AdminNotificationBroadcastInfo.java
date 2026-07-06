package com.storix.domain.domains.notification.dto;

import com.storix.domain.domains.notification.domain.AdminNotificationType;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetAudience;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetType;

public record AdminNotificationBroadcastInfo(
        String title,
        String content,
        AdminNotificationType notificationType,
        AdminNotificationTargetAudience targetAudience,
        AdminNotificationTargetType targetType,
        Long eventTargetId,
        String targetLink,
        Long lastBroadcastUserId
) {
}
