package com.storix.infrastructure.external.fcm;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.storix.domain.domains.notification.exception.FcmSendFailedException;
import com.storix.infrastructure.external.fcm.dto.MulticastResult;
import com.storix.infrastructure.external.fcm.dto.SingleSendResult;
import com.storix.infrastructure.external.fcm.dto.TokenClassification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmSender {

    private final FirebaseMessaging firebaseMessaging;
    private final FcmSenderHelper fcmSenderHelper;

    // 단일 토큰 푸시 발송
    public SingleSendResult sendToToken(String token, Map<String, String> data) {
        // 1. data-only 메시지 빌드
        Message.Builder builder = Message.builder()
                .setToken(token)
                .setAndroidConfig(highPriorityAndroid())
                .setApnsConfig(backgroundWakeApns());
        fcmSenderHelper.putData(builder::putData, data);

        try {
            // 2. 전송 성공
            String messageId = firebaseMessaging.send(builder.build());
            log.info(">>>> [FCM] send 성공. messageId={}, token={}", messageId, fcmSenderHelper.maskToken(token));
            return SingleSendResult.success(messageId);
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode code = e.getMessagingErrorCode();
            // 3. 영구 invalid 토큰 -> caller 가 deactivate 처리하도록 invalid 반환
            if (fcmSenderHelper.isPermanentInvalid(code)) {
                log.warn(">>>> [FCM] permanent invalid token. token={}, errorCode={}", fcmSenderHelper.maskToken(token), code);
                return SingleSendResult.invalid(token);
            }
            // 4. 일시 오류 / 그 외 실패 -> throw
            log.error(">>>> [FCM] send 실패. token={}, errorCode={}, msg={}", fcmSenderHelper.maskToken(token), code, e.getMessage());
            throw FcmSendFailedException.EXCEPTION;
        }
    }

    // 멀티캐스트 발송 (한 유저의 여러 디바이스 동시 발송)
    public MulticastResult sendMulticast(List<String> tokens, Map<String, String> data) {
        // 1. 빈 토큰 short-circuit
        if (tokens == null || tokens.isEmpty()) {
            return MulticastResult.empty();
        }

        // 2. data-only + background wake 메시지 빌드
        MulticastMessage.Builder builder = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setAndroidConfig(highPriorityAndroid())
                .setApnsConfig(backgroundWakeApns());
        fcmSenderHelper.putData(builder::putData, data);

        try {
            // 3. 발송 + 응답을 invalid / success 로 분류
            BatchResponse response = firebaseMessaging.sendEachForMulticast(builder.build());
            TokenClassification classified = fcmSenderHelper.classifyTokens(tokens, response);
            log.info(">>>> [FCM] multicast 결과 success={}, failure={}, invalid={}",
                    response.getSuccessCount(), response.getFailureCount(), classified.invalidTokens().size());
            return new MulticastResult(
                    response.getSuccessCount(),
                    response.getFailureCount(),
                    classified.invalidTokens(),
                    classified.successTokens()
            );
        } catch (FirebaseMessagingException e) {
            // 4. batch 자체 실패 -> throw
            log.error(">>>> [FCM] multicast 실패. tokenCount={}, errorCode={}, msg={}",
                    tokens.size(), e.getMessagingErrorCode(), e.getMessage());
            throw FcmSendFailedException.EXCEPTION;
        }
    }

    // Android HIGH 우선순위 (data-only 메시지 즉시 배달)
    private AndroidConfig highPriorityAndroid() {
        return AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .build();
    }

    // iOS background wake (data-only 메시지로 background 상태 앱을 깨우기 위한 헤더 조합)
    private ApnsConfig backgroundWakeApns() {
        return ApnsConfig.builder()
                .putHeader("apns-priority", "5")
                .putHeader("apns-push-type", "background")
                .setAps(Aps.builder().setContentAvailable(true).build())
                .build();
    }
}
