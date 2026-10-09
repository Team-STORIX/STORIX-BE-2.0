package com.storix.domain.domains.works.domain;

import com.storix.domain.domains.adultverification.exception.NotAdultVerifiedException;

import java.util.function.BooleanSupplier;

public final class AdultContentPolicy {

    private AdultContentPolicy() {}

    public static boolean isAdultOnly(AgeClassification ageClassification) {
        return ageClassification == AgeClassification.AGE_18;
    }

    // 열람 가능 여부는 성인 작품일 때만 평가한다. 일반 작품에서 불필요한 조회를 막는다
    public static void check(boolean adultOnly, BooleanSupplier canViewAdultContent) {
        if (!adultOnly) {
            return;
        }
        if (!canViewAdultContent.getAsBoolean()) {
            throw NotAdultVerifiedException.EXCEPTION;
        }
    }

    public static void check(AgeClassification ageClassification, BooleanSupplier canViewAdultContent) {
        check(isAdultOnly(ageClassification), canViewAdultContent);
    }
}
