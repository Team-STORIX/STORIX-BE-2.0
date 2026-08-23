package com.storix.domain.domains.event.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.Set;

@Getter
@RequiredArgsConstructor
public enum PromotionType {

    PUSH("푸시 알림"),
    POPUP("팝업"),
    BANNER("배너");

    /**
     * 웹페이지가 알아야 하는 홍보 수단. (PUSH 는 웹에서 그릴 게 없어 제외)
     */
    public static final Set<PromotionType> WEB_VISIBLE_TYPES = Set.of(POPUP, BANNER);

    private final String description;
}
