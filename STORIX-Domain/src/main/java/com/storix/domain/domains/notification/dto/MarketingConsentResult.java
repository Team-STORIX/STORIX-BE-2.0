package com.storix.domain.domains.notification.dto;

import java.time.LocalDateTime;

public record MarketingConsentResult(
        boolean agreed,
        String sender,
        LocalDateTime processedAt
) {
}
