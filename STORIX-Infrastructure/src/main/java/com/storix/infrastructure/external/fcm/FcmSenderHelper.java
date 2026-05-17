package com.storix.infrastructure.external.fcm;

import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.SendResponse;
import com.storix.infrastructure.external.fcm.dto.TokenClassification;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Component
public class FcmSenderHelper {

    // data payload put
    public void putData(BiConsumer<String, String> putter, Map<String, String> data) {
        if (data == null) return;
        data.forEach((k, v) -> {
            // null value entry 는 skip
            if (v != null) putter.accept(k, v);
        });
    }

    // 응답 토큰 분류 (일시 오류는 어디에도 포함 X)
    public TokenClassification classifyTokens(List<String> tokens, BatchResponse response) {
        List<SendResponse> responses = response.getResponses();
        List<String> invalid = new ArrayList<>();
        List<String> success = new ArrayList<>();
        for (int i = 0; i < responses.size(); i++) {
            SendResponse r = responses.get(i);
            // 1. 성공
            if (r.isSuccessful()) {
                success.add(tokens.get(i));
                continue;
            }
            // 2. 영구 invalid 만 invalid 그룹으로 (일시 오류는 둘 다 X)
            MessagingErrorCode code = r.getException() != null ? r.getException().getMessagingErrorCode() : null;
            if (isPermanentInvalid(code)) {
                invalid.add(tokens.get(i));
            }
        }
        return new TokenClassification(invalid, success);
    }

    // 영구 invalid 판별 (Firebase 공식 가이드 기준)
    public boolean isPermanentInvalid(MessagingErrorCode code) {
        return code == MessagingErrorCode.UNREGISTERED
                || code == MessagingErrorCode.INVALID_ARGUMENT
                || code == MessagingErrorCode.SENDER_ID_MISMATCH;
    }

    // 로깅용 토큰 마스킹
    public String maskToken(String token) {
        if (token == null || token.length() < 12) return "***";
        return token.substring(0, 6) + "..." + token.substring(token.length() - 4);
    }
}
