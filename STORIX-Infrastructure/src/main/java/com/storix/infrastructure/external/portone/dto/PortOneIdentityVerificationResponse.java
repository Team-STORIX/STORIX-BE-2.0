package com.storix.infrastructure.external.portone.dto;

import com.storix.domain.domains.adultverification.domain.IdentityVerificationStatus;
import com.storix.domain.domains.adultverification.dto.IdentityVerificationResult;

import java.time.OffsetDateTime;
import java.time.ZoneId;

public record PortOneIdentityVerificationResponse(
        String status,
        String id,
        OffsetDateTime verifiedAt,
        String pgTxId,
        PortOneVerifiedCustomer verifiedCustomer,
        PortOneIdentityVerificationFailure failure
) {

    public IdentityVerificationResult toResult() {
        return new IdentityVerificationResult(
                id,
                IdentityVerificationStatus.from(status),
                verifiedCustomer == null ? null : verifiedCustomer.birthDate(),
                // 오프셋이 붙어 오므로 그냥 자르면 어긋난다. 같은 순간으로 옮긴다
                verifiedAt == null ? null : verifiedAt.atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(),
                pgTxId,
                failure == null ? null : failure.reason(),
                failure == null ? null : failure.pgCode(),
                failure == null ? null : failure.pgMessage()
        );
    }
}
