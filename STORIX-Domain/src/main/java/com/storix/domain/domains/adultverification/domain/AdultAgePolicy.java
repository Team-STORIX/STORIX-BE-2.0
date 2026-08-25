package com.storix.domain.domains.adultverification.domain;

import java.time.LocalDate;

public final class AdultAgePolicy {

    public static final int ADULT_AGE = 19;

    private AdultAgePolicy() {}

    public static boolean isAdult(LocalDate birthDate, LocalDate baseDate) {
        if (birthDate == null || baseDate == null) {
            return false;
        }

        // 청소년보호법 제2조1항. 만 나이가 아니라 19세 되는 해 1월 1일부터 성인
        return baseDate.getYear() - birthDate.getYear() >= ADULT_AGE;
    }
}
