package com.storix.infrastructure.external.fcm.dto;

// 단일 토큰 전송 결과.
// invalidToken != null 이면 영구 invalid 로 판별되어 caller 가 deactivate 해야 함.
public record SingleSendResult(String messageId, String invalidToken) {

    public static SingleSendResult success(String messageId) {
        return new SingleSendResult(messageId, null);
    }

    public static SingleSendResult invalid(String token) {
        return new SingleSendResult(null, token);
    }
}
