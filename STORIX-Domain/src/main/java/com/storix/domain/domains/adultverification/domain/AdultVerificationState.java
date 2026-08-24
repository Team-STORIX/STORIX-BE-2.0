package com.storix.domain.domains.adultverification.domain;

public enum AdultVerificationState {
    NOT_VERIFIED,
    VERIFIED,
    EXPIRED;

    public boolean canVerify() {
        return this != VERIFIED;
    }
}
