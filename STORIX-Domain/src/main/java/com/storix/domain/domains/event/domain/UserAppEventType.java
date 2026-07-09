package com.storix.domain.domains.event.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserAppEventType {

    TITLE_ACQUIRED("칭호 획득");

    private final String description;
}
