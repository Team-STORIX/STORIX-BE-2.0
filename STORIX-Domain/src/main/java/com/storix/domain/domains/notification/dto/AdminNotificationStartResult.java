package com.storix.domain.domains.notification.dto;

import com.storix.domain.domains.notification.domain.AdminNotificationStatus;

public record AdminNotificationStartResult(
        boolean started,
        AdminNotificationStatus status
) {
}
