package com.storix.domain.domains.notification.event;

import com.storix.domain.domains.notification.domain.AdminNotificationType;
import com.storix.domain.domains.notification.domain.AdminNotificationTargetType;

import java.util.List;

public record AdminNotificationChunkEvent(
        Long adminNotificationId,
        String title,
        String content,
        AdminNotificationType notificationType,
        AdminNotificationTargetType targetType,
        Long eventTargetId,
        String targetLink,
        List<Long> userIds
) {
}
