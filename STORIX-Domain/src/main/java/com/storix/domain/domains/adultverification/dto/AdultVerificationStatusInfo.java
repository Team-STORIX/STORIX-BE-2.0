package com.storix.domain.domains.adultverification.dto;

import com.storix.domain.domains.adultverification.domain.AdultVerificationState;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record AdultVerificationStatusInfo(
        AdultVerificationState state,
        LocalDateTime verifiedAt,
        LocalDate expiresAt
) {
    public static AdultVerificationStatusInfo notVerified() {
        return new AdultVerificationStatusInfo(AdultVerificationState.NOT_VERIFIED, null, null);
    }

    public static AdultVerificationStatusInfo verified(LocalDateTime verifiedAt, LocalDate expiresAt) {
        return new AdultVerificationStatusInfo(AdultVerificationState.VERIFIED, verifiedAt, expiresAt);
    }

    // 만료된 건도 마지막 인증 시점을 보여준다
    public static AdultVerificationStatusInfo expired(LocalDateTime verifiedAt, LocalDate expiresAt) {
        return new AdultVerificationStatusInfo(AdultVerificationState.EXPIRED, verifiedAt, expiresAt);
    }
}
