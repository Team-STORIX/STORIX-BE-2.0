package com.storix.domain.domains.works.domain;

import com.storix.domain.domains.adultverification.domain.AdultVerificationPolicy;
import com.storix.domain.domains.adultverification.exception.NotAdultVerifiedException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.Supplier;

public final class AdultContentPolicy {

    private AdultContentPolicy() {}

    public static boolean isAdultOnly(AgeClassification ageClassification) {
        return ageClassification == AgeClassification.AGE_18;
    }

    // verifiedAt 은 성인 작품일 때만 평가한다. 일반 작품에서 불필요한 조회를 막는다
    public static void check(boolean adultOnly, Supplier<LocalDateTime> verifiedAt) {
        if (!adultOnly) {
            return;
        }
        if (!AdultVerificationPolicy.isValidOn(verifiedAt.get(), LocalDate.now())) {
            throw NotAdultVerifiedException.EXCEPTION;
        }
    }

    public static void check(AgeClassification ageClassification, Supplier<LocalDateTime> verifiedAt) {
        check(isAdultOnly(ageClassification), verifiedAt);
    }
}
