package com.storix.infrastructure.external.aichat.dto;

public record AiServerProgressEvent(
        String step,
        String message
) {
}
