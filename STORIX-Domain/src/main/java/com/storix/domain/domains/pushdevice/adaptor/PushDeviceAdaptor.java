package com.storix.domain.domains.pushdevice.adaptor;

import com.storix.domain.domains.pushdevice.domain.PushDevice;
import com.storix.domain.domains.pushdevice.dto.ActivePushToken;
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

    public List<ActivePushToken> findMarketingEnabledActiveTokensByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        return pushDeviceRepository.findMarketingEnabledActiveTokensByUserIds(userIds);
    }

    public List<ActivePushToken> findActiveTokensByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return List.of();
        return pushDeviceRepository.findActiveTokensByUserIds(userIds);
    }

    /** 쓰기 작업 관련 메서드 */
    // [PushDevice] 사용자 디바이스 및 FCM 토큰 등록 (upsert)
    @Transactional
    public boolean upsert(Long userId, SyncDeviceCommand cmd) {
        // 1. (userId, installationId) 기존 row 조회
        Optional<PushDevice> existing = pushDeviceRepository.findByUserIdAndInstallationId(userId, cmd.installationId());

        // 2-1. 기존 row 있으면 갱신 (dirty checking), 2-2. 없으면 등록
        boolean created;
        if (existing.isPresent()) {
            existing.get().refresh(cmd);
            created = false;
        } else {
            pushDeviceRepository.save(PushDevice.from(userId, cmd));
            created = true;
        }

        // 3. 한 기기 = 한 활성 계정 강제
        if (created) {
            pushDeviceRepository.deactivateOtherUsersOnDevice(userId, cmd.installationId());
        }

        // 4. 같은 FCM 토큰을 들고 있는 다른 활성 디바이스 행 비활성화 (installationId 재발급 될 수 있는 경우)
        pushDeviceRepository.deactivateOtherActiveDevicesByToken(userId, cmd.installationId(), cmd.fcmToken());
        return created;
    }

    // [PushDevice] 단일 디바이스 비활성화
    public int deactivateByUserIdAndInstallationId(Long userId, String installationId) {
        return pushDeviceRepository.deactivateByUserIdAndInstallationId(userId, installationId);
    }

    // [PushDevice] 유저 탈퇴 시 모든 디바이스 일괄 비활성화
    public void deactivateAllByUserId(Long userId) {
        pushDeviceRepository.deactivateAllByUserId(userId);
    }

    // [PushDispatch] FCM invalid 토큰 일괄 비활성화
    public int deactivateByFcmTokens(List<String> tokens) {
        return pushDeviceRepository.deactivateByFcmTokens(tokens);
    }

    // [PushDevice] 같은 FCM 토큰을 들고 있는 다른 활성 디바이스 행 비활성화
    public int deactivateOtherActiveDevicesByToken(Long userId, String installationId, String fcmToken) {
        return pushDeviceRepository.deactivateOtherActiveDevicesByToken(userId, installationId, fcmToken);
    }

    // [PushDispatch] 발송 성공한 토큰들의 lastSuccessAt 일괄 갱신
    public void markFcmTokensSuccess(List<String> tokens, LocalDateTime now) {
        pushDeviceRepository.markFcmTokensSuccess(tokens, now);
    }
}
