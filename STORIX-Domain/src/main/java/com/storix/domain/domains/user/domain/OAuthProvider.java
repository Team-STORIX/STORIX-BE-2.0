package com.storix.domain.domains.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OAuthProvider {

    KAKAO("카카오"),
    NAVER("네이버"),
    APPLE("애플"),
    SLACK("슬랙");

    private final String dbValue;
}
