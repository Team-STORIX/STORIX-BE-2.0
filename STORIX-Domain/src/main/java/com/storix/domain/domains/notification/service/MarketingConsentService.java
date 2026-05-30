package com.storix.domain.domains.notification.service;

import com.storix.common.utils.STORIXStatic;
import com.storix.domain.domains.notification.adaptor.NotificationSettingAdaptor;
import com.storix.domain.domains.notification.domain.NotificationSetting;
import com.storix.domain.domains.notification.dto.MarketingConsentResult;
import com.storix.domain.domains.user.adaptor.UserHistoryAdaptor;
import com.storix.domain.domains.user.domain.UserHistory;
import com.storix.domain.domains.user.domain.UserHistoryType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class MarketingConsentService {

    private final NotificationSettingAdaptor notificationSettingAdaptor;
    private final UserHistoryAdaptor userHistoryAdaptor;

    // 마케팅 수신 동의/거부 처리
    @Transactional
    public MarketingConsentResult process(Long userId, boolean agreed) {

        // 1. 알림 설정 갱신
        NotificationSetting setting = notificationSettingAdaptor.getByUserId(userId);
        setting.changeMarketing(agreed);

        // 2. 유저 로그 저장
        UserHistory saved = userHistoryAdaptor.save(UserHistory.builder()
                .userId(userId)
                .historyType(agreed ? UserHistoryType.MARKETING_AGREE : UserHistoryType.MARKETING_REJECT)
                .processor(STORIXStatic.UserHistory.PROCESSOR_TEAM_STORIX)
                .processedAt(LocalDateTime.now())
                .build());

        return new MarketingConsentResult(agreed, saved.getProcessor(), saved.getProcessedAt());
    }
}
