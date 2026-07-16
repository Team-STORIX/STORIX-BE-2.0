package com.storix.infrastructure.external.notification.dto;

public record SingleSendResult(String messageId, String invalidToken) {

    public static SingleSendResult success(String messageId) {
        return new SingleSendResult(messageId, null);
    }

    public static SingleSendResult invalid(String token) {
        return new SingleSendResult(null, token);
    }
}
