package com.storix.storix_api.global.apiPayload.exception;

import lombok.Builder;

@Builder
public record FieldErrorResponse(
        String field,
        Object rejectedValue,
        String reason,
        String message
) {}
