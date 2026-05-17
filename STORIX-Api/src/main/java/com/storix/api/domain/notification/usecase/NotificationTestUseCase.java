package com.storix.api.domain.notification.usecase;

import com.storix.api.domain.notification.controller.dto.FcmSendRequest;
import com.storix.api.domain.notification.controller.dto.NotificationDispatchTestRequest;
import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.notification.exception.FcmSendFailedException;
import com.storix.domain.domains.notification.publisher.NotificationPublisher;
import com.storix.domain.domains.pushdevice.service.PushDispatchService;
import com.storix.infrastructure.external.fcm.FcmSender;
import com.storix.infrastructure.external.fcm.dto.SingleSendResult;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@UseCase
@RequiredArgsConstructor
public class NotificationTestUseCase {

    private final PushDispatchService pushDispatchService;

    private final NotificationPublisher notificationPublisher;
    private final FcmSender fcmSender;


    // [test] FCM 환경변수 검증용 (data-only)
    public CustomResponse<String> sendTestPush(FcmSendRequest request) {
        // 1. data payload — title/body 도 data 에 담아 보냄 (data-only 정책)
        Map<String, String> data = new HashMap<>();
        data.put("title", request.title());
        data.put("body", request.body());

        // 2. FCM 알림 전송
        SingleSendResult result = fcmSender.sendToToken(request.token(), data);

        // 2-1. FCM 알림 전송에 invalid FCM 토큰이 포함되어 있었을 경우 > FCM 토큰 비활성화
        if (result.invalidToken() != null) {
            pushDispatchService.deactivateTokens(List.of(result.invalidToken()));
            throw FcmSendFailedException.EXCEPTION;
        }

        // 2-2. FCM 알림 전송 성공 시 lastSuccessAt 갱신 (추후 장기 미사용 토큰 청소용)
        pushDispatchService.markTokensSuccess(List.of(request.token()));
        return CustomResponse.onSuccess(SuccessCode.NOTIFICATION_TEST_PUSH_SUCCESS, result.messageId());
    }

    // [test] 단일 유저
    @Transactional
    public CustomResponse<Void> testDispatch(NotificationDispatchTestRequest request) {
        notificationPublisher.publish(request.toEvent());
        return CustomResponse.onSuccess(SuccessCode.NOTIFICATION_TEST_PUSH_SUCCESS);
    }
}
