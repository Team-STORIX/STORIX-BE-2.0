package com.storix.infrastructure.external.aichat.dto;

public record AiServerUsage(
        String model,
        int inputTokens,
        int outputTokens,
        int cacheReadTokens
) {
}
