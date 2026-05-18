package com.storix.domain.domains.pushdevice.service;

import com.storix.domain.domains.pushdevice.adaptor.PushDeviceAdaptor;
import com.storix.domain.domains.pushdevice.domain.PushDevice;
import com.storix.domain.domains.pushdevice.dto.SyncDeviceCommand;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 사용자 디바이스 관련 서비스
 * */
@Slf4j
@Service
@RequiredArgsConstructor
public class PushDeviceService {

    private final PushDeviceAdaptor pushDeviceAdaptor;

    // 단일 디바이스 푸시 알림 상태 동기화
    public boolean sync(Long userId, SyncDeviceCommand cmd) {
        try {
            // 1-1. 단일 디바이스 정보 upsert
            return pushDeviceAdaptor.upsert(userId, cmd);
        } catch (DataIntegrityViolationException e) {
            // 1-2. (userId, installationId) unique constraint 충돌 시 > 재시도 (update)
            log.warn(">>>> [PushDevice] register race detected, retrying. userId={}, installationId={}",
                    userId, cmd.installationId());
            return pushDeviceAdaptor.upsert(userId, cmd);
        }
    }

    // 단일 디바이스 비활성화
    @Transactional
    public int unregister(Long userId, String installationId) {
        return pushDeviceAdaptor.deactivateByUserIdAndInstallationId(userId, installationId);
    }

    // FCM 토큰 갱신
    @Transactional
    public void refreshFcmToken(Long userId, String installationId, String fcmToken) {
        PushDevice device = pushDeviceAdaptor.getByUserIdAndInstallationId(userId, installationId);
        device.refreshFcmToken(fcmToken);
    }
}
