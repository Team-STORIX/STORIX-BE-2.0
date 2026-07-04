package com.storix.domain.domains.pushdevice.dto;

public record ActivePushToken(
        Long userId,
        String fcmToken
) {
}
