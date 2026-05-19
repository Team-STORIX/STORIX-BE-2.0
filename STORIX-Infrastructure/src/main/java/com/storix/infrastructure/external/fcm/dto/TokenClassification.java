package com.storix.infrastructure.external.fcm.dto;

import java.util.List;

// FCM multicast 응답에서 무효 토큰 / 성공 토큰 분류 결과
public record TokenClassification(
        List<String> invalidTokens,
        List<String> successTokens
) {
}
