package com.storix.domain.domains.event.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PromotionType {

    PUSH("푸시 알림"),
    POPUP("팝업"),
    BANNER("배너");

    private final String description;
}
