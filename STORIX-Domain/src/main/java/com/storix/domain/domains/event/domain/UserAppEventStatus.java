package com.storix.domain.domains.event.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserAppEventStatus {

    PENDING("대기 중"),
    ACK("확인 완료");

    private final String description;
}
