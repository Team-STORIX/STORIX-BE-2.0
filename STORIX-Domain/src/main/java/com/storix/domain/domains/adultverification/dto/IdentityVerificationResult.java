package com.storix.domain.domains.adultverification.dto;

import com.storix.domain.domains.adultverification.domain.IdentityVerificationStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record IdentityVerificationResult(
        String identityVerificationId,
        IdentityVerificationStatus status,
        LocalDate birthDate,
        LocalDateTime verifiedAt,
        String providerTransactionId,

        // status 가 FAILED 일 때만
        String failureReason,
        String failurePgCode,
        String failurePgMessage
) {
    public boolean isVerified() {
        return status == IdentityVerificationStatus.VERIFIED;
    }
}
