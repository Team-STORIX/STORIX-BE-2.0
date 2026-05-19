package com.storix.infrastructure.external.fcm.dto;

import java.util.Collections;
import java.util.List;

// 멀티캐스트 전송 결과.
// invalidTokens 는 영구 invalid 로 분류된 토큰, successTokens 는 성공 토큰.
// 일시 오류 토큰은 어느 쪽에도 포함되지 않음.
public record MulticastResult(
        int successCount,
        int failureCount,
        List<String> invalidTokens,
        List<String> successTokens
) {
    public static MulticastResult empty() {
        return new MulticastResult(0, 0, Collections.emptyList(), Collections.emptyList());
    }
}
