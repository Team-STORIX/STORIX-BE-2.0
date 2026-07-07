package com.storix.domain.domains.event.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum BannerStatus {

    SCHEDULED("예약됨"),
    ACTIVE("노출 중"),
    ENDED("종료됨");

    private final String description;
}
