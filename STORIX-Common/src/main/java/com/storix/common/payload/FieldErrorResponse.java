package com.storix.common.payload;

import lombok.Builder;

@Builder
public record FieldErrorResponse(
        String field,
        Object rejectedValue,
        String reason,
        String message
) {}
