package com.storix.domain.domains.user.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TermsType {

    SERVICE("서비스 이용약관"),
    PRIVACY("개인정보 처리 방침");

    private final String description;
}
