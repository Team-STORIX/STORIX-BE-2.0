package com.storix.domain.domains.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminNotificationType {

    MARKETING(NotificationType.MARKETING),
    FEATURE_UPDATE(NotificationType.FEATURE_UPDATE),
    TOS_UPDATE(NotificationType.TOS_UPDATE),
    PRIVACY_UPDATE(NotificationType.PRIVACY_UPDATE);

    private final NotificationType notificationType;
}
