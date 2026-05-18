package com.storix.domain.domains.pushdevice.adaptor;

import com.storix.domain.domains.pushdevice.domain.PushDevice;
import com.storix.domain.domains.pushdevice.dto.SyncDeviceCommand;
import com.storix.domain.domains.pushdevice.exception.UnknownPushDeviceException;
import com.storix.domain.domains.pushdevice.repository.PushDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
@RequiredArgsConstructor
public class PushDeviceAdaptor {

    private final PushDeviceRepository pushDeviceRepository;

    /** 조회 작업 관련 메서드 */
    // [PushDevice] FCM 토큰 갱신
    public PushDevice getByUserIdAndInstallationId(Long userId, String installationId) {
        // (userId, installationId) 기존 row 없을 경우 예외 처리
        return pushDeviceRepository.findByUserIdAndInstallationId(userId, installationId)
                .orElseThrow(() -> UnknownPushDeviceException.EXCEPTION);
    }

    /** 쓰기 작업 관련 메서드 */
    // [PushDevice] 사용자 디바이스 및 FCM 토큰 등록 (upsert)
    @Transactional
    public boolean upsert(Long userId, SyncDeviceCommand cmd) {
        // 1. (userId, installationId) 기존 row 조회
        Optional<PushDevice> existing = pushDeviceRepository.findByUserIdAndInstallationId(userId, cmd.installationId());

        // 2-1. 기존 row 있으면 갱신 (dirty checking)
        if (existing.isPresent()) {
            existing.get().refresh(cmd);
            return false;
        }

        // 2-2. 기존 row 없으면 등록
        pushDeviceRepository.save(PushDevice.from(userId, cmd));
        return true;
    }

    // [PushDevice] 단일 디바이스 비활성화
    public int deactivateByUserIdAndInstallationId(Long userId, String installationId) {
        return pushDeviceRepository.deactivateByUserIdAndInstallationId(userId, installationId);
    }

    // [PushDevice] 유저 탈퇴 시 모든 디바이스 일괄 비활성화
    public int deactivateAllByUserId(Long userId) {
        return pushDeviceRepository.deactivateAllByUserId(userId);
    }
}
