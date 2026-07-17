package com.storix.domain.domains.event.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Getter
@RequiredArgsConstructor
public enum AppEventStatus {

    SCHEDULED("예약됨"),
    ACTIVE("진행 중"),
    ENDED("종료됨");

    private final String description;

    public static AppEventStatus resolve(LocalDateTime startAt, LocalDateTime endAt, LocalDateTime now) {
        if (!now.isBefore(endAt)) {
            return ENDED;
        }
        if (now.isBefore(startAt)) {
            return SCHEDULED;
        }
        return ACTIVE;
    }
}
