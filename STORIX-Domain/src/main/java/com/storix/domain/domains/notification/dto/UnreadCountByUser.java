package com.storix.domain.domains.notification.dto;

public record UnreadCountByUser(
        Long userId,
        Long count
) {
}
