package com.storix.domain.domains.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminNotificationStatus {

    SCHEDULED("발송 예정"),
    SENDING("발송 중"),
    SENT("발송 완료"),
    FAILED("발송 실패"),
    CANCELED("취소");

    private final String description;
}
