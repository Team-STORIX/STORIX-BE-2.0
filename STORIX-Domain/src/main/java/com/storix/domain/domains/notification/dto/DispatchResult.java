package com.storix.domain.domains.notification.dto;

import java.util.List;

// 알림 디스패치 결과 — 푸시 발송 여부와 대상 토큰을 Infra 단(Listener)에 전달
public record DispatchResult(
        Long notificationId,
        List<String> tokens,
        boolean shouldSendPush
) {
    // 전부 skip — DELETED 유저거나 타입 토글 OFF
    public static DispatchResult skip() {
        return new DispatchResult(null, List.of(), false);
    }

    // 인앱 알림만 저장, 푸시는 skip — SUSPENDED 유저의 일반 알림
    public static DispatchResult inAppOnly(Long notificationId) {
        return new DispatchResult(notificationId, List.of(), false);
    }

    // 인앱 + 푸시 발송
    public static DispatchResult pushTo(Long notificationId, List<String> tokens) {
        return new DispatchResult(notificationId, tokens, !tokens.isEmpty());
    }
}
