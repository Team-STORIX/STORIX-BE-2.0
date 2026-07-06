package com.storix.infrastructure.external.notification.fcm.helper;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import com.storix.infrastructure.external.notification.dto.TokenClassification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Firebase 응답 토큰 분류
 *  - 토큰 삭제 : UNREGISTERED(400) / INVALID_ARGUMENT(400)
 *              -> 토큰 비활성화 (재시도 대상 X)
 *  - 일시 오류 : UNAVAILABLE(503) / INTERNAL(500) / QUOTA_EXCEEDED(429)
 *              -> 지수 백오프 재시도 대상
 *  - 영구·설정 오류 : SENDER_ID_MISMATCH(403) / THIRD_PARTY_AUTH_ERROR(401) / UNSPECIFIED_ERROR
 *              -> 모니터링 + 수동 재시도 대상
 */
@Component
public class FcmErrorClassifier {

    public TokenClassification classifyTokens(List<String> tokens, BatchResponse response) {
        List<SendResponse> responses = response.getResponses();
        List<String> invalid = new ArrayList<>();
        List<String> success = new ArrayList<>();
        List<MessagingErrorCode> failureCodes = new ArrayList<>();
        boolean hasTransient = false;

        for (int i = 0; i < responses.size(); i++) {
            SendResponse r = responses.get(i);
            // 1. 성공
            if (r.isSuccessful()) {
                success.add(tokens.get(i));
                continue;
            }

            // 2. 실패 → 코드별 분류
            MessagingErrorCode code = r.getException() != null ? r.getException().getMessagingErrorCode() : null;
            failureCodes.add(code);
            if (isDeletableToken(code)) {
                invalid.add(tokens.get(i));
            } else if (isRetryableToken(code)) {
                hasTransient = true;
            }
        }
        return new TokenClassification(invalid, success, hasTransient, failureCodes);
    }

    // 토큰 삭제 대상
    public boolean isDeletableToken(MessagingErrorCode code) {
        return code == MessagingErrorCode.UNREGISTERED
                || code == MessagingErrorCode.INVALID_ARGUMENT;
    }

    // 재시도 대상
    public boolean isRetryableToken(MessagingErrorCode code) {
        return code == MessagingErrorCode.UNAVAILABLE
                || code == MessagingErrorCode.INTERNAL
                || code == MessagingErrorCode.QUOTA_EXCEEDED;
    }

    // 로깅용 토큰 마스킹
    public String maskToken(String token) {
        if (token == null || token.length() < 12) return "***";
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}
