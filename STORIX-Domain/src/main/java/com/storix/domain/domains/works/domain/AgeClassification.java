package com.storix.domain.domains.works.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AgeClassification {

    ALL("전체연령가"),
    AGE_12("12세 이용가"),
    AGE_15("15세 이용가"),
    AGE_18("18세 이용가");

    private final String dbValue;
}
