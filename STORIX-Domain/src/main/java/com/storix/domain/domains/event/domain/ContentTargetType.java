package com.storix.domain.domains.event.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ContentTargetType {

    APP_EVENT("앱 이벤트");

    private final String description;
}
