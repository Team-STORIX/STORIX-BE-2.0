package com.storix.api.domain.pushdevice.usecase;

import com.storix.api.domain.pushdevice.controller.dto.DeviceSyncRequest;
import com.storix.api.domain.pushdevice.controller.dto.RefreshFcmTokenRequest;
import com.storix.common.annotation.UseCase;
import com.storix.domain.domains.pushdevice.service.PushDeviceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@UseCase
@RequiredArgsConstructor
public class PushDeviceUseCase {

    private final PushDeviceService pushDeviceService;

    // 1. 단일 디바이스 푸시 알림 상태 동기화
    public void sync(Long userId, DeviceSyncRequest request) {
        boolean isNew = pushDeviceService.sync(userId, request.toCommand());

        log.info(">>> [PushDevice] 동기화 installationId={} isNew={}", request.installationId(), isNew);
    }

    // 2. 단일 디바이스 푸시 알림 해제
    public void unregister(Long userId, String installationId) {
        int affected = pushDeviceService.unregister(userId, installationId);
        if (affected == 0) {
            log.warn(">>> [PushDevice] 해제 대상 없음 installationId={}", installationId);
            return;
        }
        log.info(">>> [PushDevice] 해제 installationId={}", installationId);
    }

    // 3. FCM 토큰 갱신
    public void refreshFcmToken(Long userId, RefreshFcmTokenRequest request) {
        pushDeviceService.refreshFcmToken(userId, request.installationId(), request.fcmToken());
        log.info(">>> [PushDevice] FCM 토큰 갱신 installationId={}", request.installationId());
    }
}
