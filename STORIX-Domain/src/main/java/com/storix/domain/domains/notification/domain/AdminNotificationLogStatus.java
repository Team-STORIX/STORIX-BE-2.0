package com.storix.domain.domains.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminNotificationLogStatus {

    PENDING("발송 대기 / 재시도 대상"),
    SENT("발송 완료"),
    SKIPPED("발송 대상 외"),
    FAILED("발송 실패");

    private final String description;
}
