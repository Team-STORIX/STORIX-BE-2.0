package com.storix.infrastructure.external.notification.dto;

import java.util.Map;

public record PushMessage(
        String token,
        Map<String, String> data,
        boolean androidDataOnly
) {
}
