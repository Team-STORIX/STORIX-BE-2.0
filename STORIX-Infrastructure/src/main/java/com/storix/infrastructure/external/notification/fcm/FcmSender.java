package com.storix.infrastructure.external.notification.fcm;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.storix.infrastructure.external.notification.dto.MulticastResult;
import com.storix.infrastructure.external.notification.dto.SingleSendResult;
import com.storix.infrastructure.external.notification.dto.TokenClassification;
import com.storix.infrastructure.external.notification.exception.FcmSendFailedException;
import com.storix.infrastructure.external.notification.exception.FcmTransientException;
import com.storix.common.utils.STORIXStatic;
import com.storix.infrastructure.external.notification.fcm.helper.FcmErrorClassifier;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmSender {

    private static final String METRIC_FAILURE = "fcm.send.failure";
    private static final String TAG_CODE = "code";

    private final FirebaseMessaging firebaseMessaging;
    private final FcmErrorClassifier fcmErrorClassifier;

    private final MeterRegistry meterRegistry;

    // 단일 토큰 푸시 발송
    public SingleSendResult sendToToken(String token, Map<String, String> data) {
        // 1. 화면 표시용 notification + 라우팅용 data 메시지 빌드
        Message.Builder builder = Message.builder()
                .setToken(token)
                .setNotification(displayNotification(data))
                .setAndroidConfig(highPriorityAndroid())
                .setApnsConfig(apnsConfig(data));
        putData(builder::putData, data);

        try {
            // 2. 전송 성공
            String messageId = firebaseMessaging.send(builder.build());
            log.debug(">>>> [FCM] send 성공. messageId={}, token={}", messageId, fcmErrorClassifier.maskToken(token));
            return SingleSendResult.success(messageId);
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode code = e.getMessagingErrorCode();
            recordFailureMetric(code);
            // 3. 삭제 대상 토큰(UNREGISTERED/INVALID_ARGUMENT) -> caller 가 deactivate 처리하도록 invalid 반환
            if (fcmErrorClassifier.isDeletableToken(code)) {
                log.warn(">>>> [FCM] deletable token. token={}, errorCode={}", fcmErrorClassifier.maskToken(token), code);
                return SingleSendResult.invalid(token);
            }
            // 4. 일시 오류(UNAVAILABLE/INTERNAL/QUOTA_EXCEEDED) -> 재시도 대상 (multicast 와 분류 일치)
            if (fcmErrorClassifier.isRetryableToken(code)) {
                log.warn(">>>> [FCM] send 일시 실패(재시도 대상). token={}, errorCode={}, msg={}",
                        fcmErrorClassifier.maskToken(token), code, e.getMessage());
                throw new FcmTransientException(code, e);
            }
            // 5. 영구·설정 오류 -> 재시도 무의미
            log.error(">>>> [FCM] send 영구 실패. token={}, errorCode={}, msg={}",
                    fcmErrorClassifier.maskToken(token), code, e.getMessage());
            throw FcmSendFailedException.EXCEPTION;
        }
    }

    // 멀티캐스트 발송 (한 유저의 여러 디바이스 동시 발송)
    public MulticastResult sendMulticast(List<String> tokens, Map<String, String> data) {
        // 1. 빈 토큰 short-circuit
        if (tokens == null || tokens.isEmpty()) {
            return MulticastResult.empty();
        }

        // 2. 화면 표시용 notification + 라우팅용 data 메시지 빌드
        MulticastMessage.Builder builder = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(displayNotification(data))
                .setAndroidConfig(highPriorityAndroid())
                .setApnsConfig(apnsConfig(data));
        putData(builder::putData, data);

        try {
            // 3. 발송 + 응답을 invalid / success 로 분류
            BatchResponse response = firebaseMessaging.sendEachForMulticast(builder.build());
            TokenClassification classified = fcmErrorClassifier.classifyTokens(tokens, response);
            classified.failureCodes().forEach(this::recordFailureMetric); // 코드별 실패 지표 (알림 토대)
            log.info(">>>> [FCM] multicast 결과 success={}, failure={}, invalid={}, transient={}, codes={}",
                    response.getSuccessCount(), response.getFailureCount(),
                    classified.invalidTokens().size(), classified.hasTransientFailure(), classified.failureCodes());
            return new MulticastResult(
                    response.getSuccessCount(),
                    response.getFailureCount(),
                    classified.invalidTokens(),
                    classified.successTokens(),
                    classified.hasTransientFailure()
            );
        } catch (FirebaseMessagingException e) {
            // 4. batch 자체 실패 -> 에러코드로 일시/영구 분류 (일시만 재시도 대상)
            MessagingErrorCode code = e.getMessagingErrorCode();
            recordFailureMetric(code);
            if (fcmErrorClassifier.isRetryableToken(code)) {
                log.warn(">>>> [FCM] multicast 일시 실패(재시도 대상). tokenCount={}, errorCode={}, msg={}",
                        tokens.size(), code, e.getMessage());
                throw new FcmTransientException(code, e);  // UNAVAILABLE/INTERNAL/QUOTA_EXCEEDED → 재시도
            }
            log.error(">>>> [FCM] multicast 영구 실패. tokenCount={}, errorCode={}, msg={}",
                    tokens.size(), code, e.getMessage());
            throw FcmSendFailedException.EXCEPTION;          // 영구·설정 오류 → 재시도 무의미
        }
    }

    // 실패 코드별 카운터 누적 (code=null 은 UNSPECIFIED 로 표기)
    private void recordFailureMetric(MessagingErrorCode code) {
        meterRegistry.counter(METRIC_FAILURE, TAG_CODE, code != null ? code.name() : "UNSPECIFIED").increment();
    }

    // data payload put (null value entry 는 skip)
    private void putData(BiConsumer<String, String> putter, Map<String, String> data) {
        if (data == null) return;
        data.forEach((k, v) -> {
            if (v != null) putter.accept(k, v);
        });
    }

    private Notification displayNotification(Map<String, String> data) {
        return Notification.builder()
                .setTitle(data.get("title"))
                .setBody(data.get("body"))
                .build();
    }

    // Android HIGH 전송 우선순위 + 알림 표시 우선순위 MAX(헤드업 유도) + 기본 사운드
    private AndroidConfig highPriorityAndroid() {
        return AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(AndroidNotification.builder()
                        .setChannelId(STORIXStatic.Notification.ANDROID_CHANNEL_ID)
                        .setDefaultSound(true)
                        .setPriority(AndroidNotification.Priority.MAX)
                        .build())
                .build();
    }

    // iOS APNs 고우선순위 즉시 표시(alert) + 기본 사운드 + 뱃지(미읽음 총합)
    private ApnsConfig apnsConfig(Map<String, String> data) {
        Aps.Builder aps = Aps.builder().setSound("default");
        Integer badge = parseBadge(data);
        if (badge != null) {
            aps.setBadge(badge);
        }
        return ApnsConfig.builder()
                .putHeader("apns-priority", "10")
                .putHeader("apns-push-type", "alert")
                .setAps(aps.build())
                .build();
    }

    // data.unreadCount -> iOS 뱃지 숫자 (없거나 파싱 실패 시 뱃지 미설정)
    private Integer parseBadge(Map<String, String> data) {
        if (data == null) return null;
        String value = data.get("unreadCount");
        if (value == null) return null;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
