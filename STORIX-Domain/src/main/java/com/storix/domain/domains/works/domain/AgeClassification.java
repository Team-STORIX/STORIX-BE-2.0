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

    // 선언 순서가 연령 순서. 비어 있는 쪽은 무시
    public static AgeClassification higher(AgeClassification current, AgeClassification incoming) {
        if (current == null) return incoming;
        if (incoming == null) return current;
        return incoming.compareTo(current) > 0 ? incoming : current;
    }
}
