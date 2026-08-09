package com.storix.infrastructure.external.notification.fcm;

import com.storix.domain.domains.pushdevice.service.PushDispatchService;
import com.storix.infrastructure.external.notification.dto.MulticastResult;
import com.storix.infrastructure.external.notification.dto.PushMessage;
import com.storix.infrastructure.external.notification.exception.FcmTransientException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmPushExecutor {

    private static final int MAX_SEND_ATTEMPTS = 3;
    private static final long BASE_BACKOFF_MS = 500L;
    private static final int MAX_BATCH_SIZE = 500;

    private final FcmSender fcmSender;
    private final PushDispatchService pushDispatchService;

    public MulticastResult sendAndApply(List<String> tokens, Map<String, String> data) {
        return sendAndApply(tokens, data, null);
    }

    public MulticastResult sendAndApply(List<String> tokens, Map<String, String> data, String collapseKey) {
        MulticastResult result = fcmSender.sendMulticast(tokens, data, collapseKey);
        if (!result.invalidTokens().isEmpty()) {
            pushDispatchService.deactivateTokens(result.invalidTokens());
        }
        if (!result.successTokens().isEmpty()) {
            pushDispatchService.markTokensSuccess(result.successTokens());
        }
        return result;
    }

    public MulticastResult sendEachAndApply(List<PushMessage> messages, String collapseKey) {
        MulticastResult result = fcmSender.sendEach(messages, collapseKey);
        if (!result.invalidTokens().isEmpty()) {
            pushDispatchService.deactivateTokens(result.invalidTokens());
        }
        if (!result.successTokens().isEmpty()) {
            pushDispatchService.markTokensSuccess(result.successTokens());
        }
        return result;
    }

    public void sendEachWithRetry(List<PushMessage> messages, String collapseKey, String logContext) {
        if (messages == null || messages.isEmpty()) return;

        for (int from = 0; from < messages.size(); from += MAX_BATCH_SIZE) {
            List<PushMessage> chunk = messages.subList(from, Math.min(from + MAX_BATCH_SIZE, messages.size()));
            retrying(() -> sendEachAndApply(chunk, collapseKey), logContext);
        }
    }

    // 일시 오류만 백오프 재시도
    public void sendWithRetry(List<String> tokens, Map<String, String> data, String collapseKey, String logContext) {
        retrying(() -> sendAndApply(tokens, data, collapseKey), logContext);
    }

    private void retrying(Supplier<MulticastResult> send, String logContext) {
        FcmTransientException lastTransient = null;

        for (int attempt = 1; attempt <= MAX_SEND_ATTEMPTS; attempt++) {
            try {
                MulticastResult result = send.get();
                if (result.hasTransientFailure()) {
                    log.warn(">>> [Fcm] 일부 토큰 일시오류 - 재시도 생략(중복발송 방지) {}, failure={}",
                            logContext, result.failureCount());
                }
                return;
            } catch (FcmTransientException e) {
                lastTransient = e;
                if (attempt < MAX_SEND_ATTEMPTS) {
                    log.warn(">>> [Fcm] 일시오류 재시도 {}/{} {}, code={}",
                            attempt, MAX_SEND_ATTEMPTS, logContext, e.getMessagingErrorCode());
                    sleep(BASE_BACKOFF_MS << (attempt - 1)); // 0.5s, 1s
                }
            } catch (Exception e) {
                log.error(">>> [Fcm] 영구 실패(재시도 안 함) {}, cause={}", logContext, e.getMessage());
                return;
            }
        }

        log.error(">>> [Fcm] 일시오류 최종 실패 - 재시도 {}회 소진 {}, code={}",
                MAX_SEND_ATTEMPTS, logContext, lastTransient.getMessagingErrorCode());
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
