package com.storix.infrastructure.external.portone.dto;

public record PortOneIdentityVerificationFailure(
        String reason,
        String pgCode,
        String pgMessage
) {
}
