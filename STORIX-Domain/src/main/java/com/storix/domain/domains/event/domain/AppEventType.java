package com.storix.domain.domains.event.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AppEventType {

    GENERAL("일반", false),
    ATTENDANCE("출석 체크", true),
    STORY_CARD("오늘의 스토리 카드", true);

    private final String description;

    // 같은 시점에 하나만 진행될 수 있는지 여부
    private final boolean exclusive;
}
