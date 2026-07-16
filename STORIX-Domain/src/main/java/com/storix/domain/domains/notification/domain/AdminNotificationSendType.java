package com.storix.domain.domains.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminNotificationSendType {

    IMMEDIATE("즉시 발송"),
    SCHEDULED("예약 발송");

    private final String description;
}
