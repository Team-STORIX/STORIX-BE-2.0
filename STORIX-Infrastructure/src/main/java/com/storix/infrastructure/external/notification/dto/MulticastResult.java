package com.storix.infrastructure.external.notification.dto;

import java.util.Collections;
import java.util.List;

public record MulticastResult(
        int successCount,
        int failureCount,
        List<String> invalidTokens,
        List<String> successTokens,
        boolean hasTransientFailure
) {
    public static MulticastResult empty() {
        return new MulticastResult(0, 0, Collections.emptyList(), Collections.emptyList(), false);
    }
}
