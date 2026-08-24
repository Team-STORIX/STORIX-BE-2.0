package com.storix.infrastructure.external.portone.dto;

import com.storix.domain.domains.adultverification.domain.IdentityVerificationStatus;
import com.storix.domain.domains.adultverification.dto.IdentityVerificationResult;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

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
                // 마이크로초로 자르는 건 DB 가 거기까지만 담기 때문이다. 응답과 저장값이 갈리지 않게 한다
                verifiedAt == null ? null
                        : verifiedAt.atZoneSameInstant(ZoneId.systemDefault())
                                .toLocalDateTime()
                                .truncatedTo(ChronoUnit.MICROS),
                pgTxId,
                failure == null ? null : failure.reason(),
                failure == null ? null : failure.pgCode(),
                failure == null ? null : failure.pgMessage()
        );
    }
}
