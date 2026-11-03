package com.storix.domain.domains.notification.event;

public record NotificationPushEvent(
        Long notificationId,
        NotificationEvent event
) {
}
