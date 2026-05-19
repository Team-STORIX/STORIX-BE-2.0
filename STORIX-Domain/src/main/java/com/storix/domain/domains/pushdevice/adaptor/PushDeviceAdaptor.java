package com.storix.domain.domains.pushdevice.adaptor;

import com.storix.domain.domains.pushdevice.domain.PushDevice;
import com.storix.domain.domains.pushdevice.dto.SyncDeviceCommand;
import com.storix.domain.domains.pushdevice.exception.UnknownPushDeviceException;
import com.storix.domain.domains.pushdevice.repository.PushDeviceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
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

    // [PushDispatch] 한 유저의 활성 디바이스 FCM 토큰 일괄 조회
    public List<String> findActiveFcmTokensByUserId(Long userId) {
        return pushDeviceRepository.findActiveFcmTokensByUserId(userId);
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

    // [PushDispatch] FCM invalid 토큰 일괄 비활성화
    public int deactivateByFcmTokens(List<String> tokens) {
        return pushDeviceRepository.deactivateByFcmTokens(tokens);
    }

    // [PushDispatch] 발송 성공한 토큰들의 lastSuccessAt 일괄 갱신
    public void markFcmTokensSuccess(List<String> tokens, LocalDateTime now) {
        pushDeviceRepository.markFcmTokensSuccess(tokens, now);
    }
}
