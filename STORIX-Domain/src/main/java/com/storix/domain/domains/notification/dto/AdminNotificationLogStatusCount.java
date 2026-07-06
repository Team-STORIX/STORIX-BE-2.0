package com.storix.domain.domains.notification.dto;

import com.storix.domain.domains.notification.domain.AdminNotificationLogStatus;

public record AdminNotificationLogStatusCount(
        AdminNotificationLogStatus status,
        Long count
) {
}
