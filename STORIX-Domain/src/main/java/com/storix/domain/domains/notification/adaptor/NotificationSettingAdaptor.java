package com.storix.domain.domains.notification.adaptor;

import com.storix.domain.domains.notification.domain.NotificationSetting;
import com.storix.domain.domains.notification.exception.UnknownNotificationSettingException;
import com.storix.domain.domains.notification.repository.NotificationSettingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationSettingAdaptor {

    private final NotificationSettingRepository notificationSettingRepository;

    /** 조회 작업 관련 메서드 */
    // 유저 알림 설정 조회 — 없으면 예외 (모든 유저는 회원가입 시 row 가 생성되어 있다는 invariant)
    public NotificationSetting getByUserId(Long userId) {
        return notificationSettingRepository.findById(userId)
                .orElseThrow(() -> UnknownNotificationSettingException.EXCEPTION);
    }


    /** 쓰기 작업 관련 메서드 */
    // 신규 유저 가입 시 기본값으로 저장
    public void save(Long userId) {
        notificationSettingRepository.save(NotificationSetting.defaultFor(userId));
    }

    // 유저 탈퇴 시 삭제
    public void deleteByUserId(Long userId) {
        notificationSettingRepository.deleteById(userId);
    }
}
