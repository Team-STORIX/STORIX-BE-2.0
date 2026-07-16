package com.storix.domain.domains.works.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Platform {

    NAVER_WEBTOON("네이버 웹툰"),
    NAVER_NOVEL("네이버 웹소설"),
    KAKAO_PAGE("카카오페이지"),
    RIDIBOOKS("리디북스"),
    BOMTOON("봄툰"),
    NAVER_SERIES("네이버 시리즈");

    private final String dbValue;
}
