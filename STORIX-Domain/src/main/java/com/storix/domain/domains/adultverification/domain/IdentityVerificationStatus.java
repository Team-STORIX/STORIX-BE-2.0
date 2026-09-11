package com.storix.domain.domains.adultverification.domain;

import com.storix.domain.domains.adultverification.exception.IdentityVerificationProviderException;

import java.util.Arrays;

public enum IdentityVerificationStatus {
    READY,
    VERIFIED,
    FAILED;

    public static IdentityVerificationStatus from(String value) {
        return Arrays.stream(values())
                .filter(status -> status.name().equals(value))
                .findFirst()
                .orElseThrow(() -> IdentityVerificationProviderException.EXCEPTION);
    }
}
