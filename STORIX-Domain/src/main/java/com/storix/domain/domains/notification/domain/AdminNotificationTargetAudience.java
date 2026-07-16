package com.storix.domain.domains.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AdminNotificationTargetAudience {

    ALL("전체 유저"),
    NEW_USERS("신규 유저 (가입 첫 달)"),
    EVENT_WINNERS("이벤트 당첨 유저");

    private final String description;
}
