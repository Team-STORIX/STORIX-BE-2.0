package com.storix.domain.domains.adultverification.dto;

public record AdultVerificationTicket(
        String identityVerificationId,
        String storeId,
        String channelKey
) {
}
