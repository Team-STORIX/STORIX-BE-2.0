package com.storix.api.domain.aichat.stream.dto;

public record AiServerProgressEvent(
        String step,
        String message
) {
}
