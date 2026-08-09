package com.storix.infrastructure.external.notification.fcm;

import com.google.firebase.messaging.AndroidConfig;
import com.google.firebase.messaging.AndroidNotification;
import com.google.firebase.messaging.ApnsConfig;
import com.google.firebase.messaging.Aps;
import com.google.firebase.messaging.ApsAlert;
import com.google.firebase.messaging.BatchResponse;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.MulticastMessage;
import com.google.firebase.messaging.Notification;
import com.storix.infrastructure.external.notification.dto.MulticastResult;
import com.storix.infrastructure.external.notification.dto.PushMessage;
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
        try {
            // 1. 전송 성공
            String messageId = firebaseMessaging.send(buildMessage(token, data, null));
            log.debug(">>>> [FCM] send 성공. messageId={}, token={}", messageId, fcmErrorClassifier.maskToken(token));
            return SingleSendResult.success(messageId);
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode code = e.getMessagingErrorCode();
            recordFailureMetric(code);
            // 2. 삭제 대상 토큰(UNREGISTERED/INVALID_ARGUMENT) -> caller 가 deactivate 처리하도록 invalid 반환
            if (fcmErrorClassifier.isDeletableToken(code)) {
                log.warn(">>>> [FCM] deletable token. token={}, errorCode={}", fcmErrorClassifier.maskToken(token), code);
                return SingleSendResult.invalid(token);
            }
            // 3. 일시 오류(UNAVAILABLE/INTERNAL/QUOTA_EXCEEDED) -> 재시도 대상 (multicast 와 분류 일치)
            if (fcmErrorClassifier.isRetryableToken(code)) {
                log.warn(">>>> [FCM] send 일시 실패(재시도 대상). token={}, errorCode={}, msg={}",
                        fcmErrorClassifier.maskToken(token), code, e.getMessage());
                throw new FcmTransientException(code, e);
            }
            // 4. 영구·설정 오류 -> 재시도 무의미
            log.error(">>>> [FCM] send 영구 실패. token={}, errorCode={}, msg={}",
                    fcmErrorClassifier.maskToken(token), code, e.getMessage());
            throw FcmSendFailedException.EXCEPTION;
        }
    }

    // 멀티캐스트 발송 (한 유저의 여러 디바이스 동시 발송)
    public MulticastResult sendMulticast(List<String> tokens, Map<String, String> data) {
        return sendMulticast(tokens, data, null);
    }

    // collapseKey 를 주면 같은 키의 이전 알림을 덮어써 트레이에 하나만 남는다
    public MulticastResult sendMulticast(List<String> tokens, Map<String, String> data, String collapseKey) {
        // 1. 빈 토큰 short-circuit
        if (tokens == null || tokens.isEmpty()) {
            return MulticastResult.empty();
        }

        // 2. 화면 표시용 notification + 라우팅용 data 메시지 빌드
        MulticastMessage.Builder builder = MulticastMessage.builder()
                .addAllTokens(tokens)
                .setNotification(displayNotification(data))
                .setAndroidConfig(highPriorityAndroid(collapseKey, data))
                .setApnsConfig(apnsConfig(data, collapseKey));
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

    // 수신자마다 payload 가 다를 때. SDK 가 한 배치를 병렬로 던진다
    public MulticastResult sendEach(List<PushMessage> messages, String collapseKey) {
        if (messages == null || messages.isEmpty()) {
            return MulticastResult.empty();
        }

        // 1. 응답이 입력 순서와 1:1 이라 토큰 목록을 같은 순서로 들고 간다
        List<String> tokens = messages.stream().map(PushMessage::token).toList();
        List<Message> payloads = messages.stream()
                .map(m -> buildMessage(m.token(), m.data(), collapseKey))
                .toList();

        try {
            // 2. 발송 + 응답을 invalid / success 로 분류
            BatchResponse response = firebaseMessaging.sendEach(payloads);
            TokenClassification classified = fcmErrorClassifier.classifyTokens(tokens, response);
            classified.failureCodes().forEach(this::recordFailureMetric);
            log.info(">>>> [FCM] sendEach 결과 success={}, failure={}, invalid={}, transient={}, codes={}",
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
            // 3. batch 자체 실패 -> 에러코드로 일시/영구 분류
            MessagingErrorCode code = e.getMessagingErrorCode();
            recordFailureMetric(code);
            if (fcmErrorClassifier.isRetryableToken(code)) {
                log.warn(">>>> [FCM] sendEach 일시 실패(재시도 대상). messageCount={}, errorCode={}, msg={}",
                        messages.size(), code, e.getMessage());
                throw new FcmTransientException(code, e);
            }
            log.error(">>>> [FCM] sendEach 영구 실패. messageCount={}, errorCode={}, msg={}",
                    messages.size(), code, e.getMessage());
            throw FcmSendFailedException.EXCEPTION;
        }
    }

    private Message buildMessage(String token, Map<String, String> data, String collapseKey) {
        Message.Builder builder = Message.builder()
                .setToken(token)
                .setNotification(displayNotification(data))
                .setAndroidConfig(highPriorityAndroid(collapseKey, data))
                .setApnsConfig(apnsConfig(data, collapseKey));
        putData(builder::putData, data);
        return builder.build();
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
    private AndroidConfig highPriorityAndroid(String collapseKey, Map<String, String> data) {
        AndroidNotification.Builder notification = AndroidNotification.builder()
                .setChannelId(STORIXStatic.Notification.ANDROID_CHANNEL_ID)
                .setDefaultSound(true)
                .setPriority(AndroidNotification.Priority.MAX);

        // 런처 배지 숫자 — 지원하는 런처에서만 표시된다
        Integer badge = parseBadge(data);
        if (badge != null) {
            notification.setNotificationCount(badge);
        }

        AndroidConfig.Builder builder = AndroidConfig.builder()
                .setPriority(AndroidConfig.Priority.HIGH)
                .setNotification(notification.build());
        if (collapseKey != null) {
            builder.setCollapseKey(collapseKey);
        }
        return builder.build();
    }

    // iOS APNs 고우선순위 즉시 표시(alert) + 기본 사운드 + 뱃지(미읽음 총합)
    private ApnsConfig apnsConfig(Map<String, String> data) {
        return apnsConfig(data, null);
    }

    private ApnsConfig apnsConfig(Map<String, String> data, String collapseKey) {
        Aps.Builder aps = Aps.builder().setSound("default");
        Integer badge = parseBadge(data);
        if (badge != null) {
            aps.setBadge(badge);
        }
        // subtitle 이 있으면 제목 2줄 + 본문. Android 는 subtitle 자리가 없어 2줄까지만 된다
        String subtitle = data != null ? data.get("subtitle") : null;
        if (subtitle != null) {
            aps.setAlert(ApsAlert.builder()
                    .setTitle(data.get("title"))
                    .setSubtitle(subtitle)
                    .setBody(data.get("body"))
                    .build());
        }
        // 앱의 Notification Service Extension 이 알림을 가로채 꾸미려면 필요
        if (data != null && Boolean.parseBoolean(data.get("mutableContent"))) {
            aps.setMutableContent(true);
        }
        // 알림센터에서 같은 스레드끼리 묶어서 쌓임
        String threadId = data != null ? data.get("threadId") : null;
        if (threadId != null) {
            aps.setThreadId(threadId);
        }
        ApnsConfig.Builder builder = ApnsConfig.builder()
                .putHeader("apns-priority", "10")
                .putHeader("apns-push-type", "alert")
                .setAps(aps.build());
        if (collapseKey != null) {
            builder.putHeader("apns-collapse-id", collapseKey);
        }
        return builder.build();
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
