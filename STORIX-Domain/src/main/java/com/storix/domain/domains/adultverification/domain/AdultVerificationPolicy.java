package com.storix.domain.domains.adultverification.domain;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class AdultVerificationPolicy {

    public static final int VALID_YEARS = 1;

    private AdultVerificationPolicy() {}

    // 인증한 날의 1년 뒤 같은 날짜까지 유효. 2월 29일은 다음 해 2월 28일로 당겨진다
    public static LocalDate expiresOn(LocalDateTime verifiedAt) {
        return verifiedAt.toLocalDate().plusYears(VALID_YEARS);
    }

    public static boolean isValidOn(LocalDateTime verifiedAt, LocalDate today) {
        return verifiedAt != null && !today.isAfter(expiresOn(verifiedAt));
    }
}
