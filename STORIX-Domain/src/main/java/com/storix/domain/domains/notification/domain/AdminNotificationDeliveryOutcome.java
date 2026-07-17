package com.storix.domain.domains.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminNotificationDeliveryOutcome {

    SENT("1개 이상 성공"),
    SKIPPED("활성 토큰 없음 또는 전부 무효 토큰"),
    TRANSIENT_FAILURE("일시 오류 - 백오프 재시도 대상"),
    PERMANENT_FAILURE("설정·영구 오류");

    private final String description;
}
