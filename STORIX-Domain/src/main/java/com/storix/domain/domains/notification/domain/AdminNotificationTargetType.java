package com.storix.domain.domains.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminNotificationTargetType {

    NONE(TargetType.NONE),
    APP_EVENT(TargetType.APP_EVENT),
    EXTERNAL(TargetType.EXTERNAL);

    private final TargetType targetType;
}
