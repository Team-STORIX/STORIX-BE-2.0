package com.storix.domain.domains.notification.dto;

public record AdminNotificationDispatchCounts(int sent, int failed, int skipped) {

    public static AdminNotificationDispatchCounts empty() {
        return new AdminNotificationDispatchCounts(0, 0, 0);
    }
}
