package com.storix.infrastructure.external.notification.dto;

import com.google.firebase.messaging.MessagingErrorCode;

import java.util.List;

public record TokenClassification(
        List<String> invalidTokens,
        List<String> successTokens,
        boolean hasTransientFailure,
        List<MessagingErrorCode> failureCodes
) {
}
