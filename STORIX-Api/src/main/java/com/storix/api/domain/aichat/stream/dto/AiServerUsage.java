package com.storix.api.domain.aichat.stream.dto;

public record AiServerUsage(
        String model,
        int inputTokens,
        int outputTokens,
        int cacheReadTokens
) {
}
