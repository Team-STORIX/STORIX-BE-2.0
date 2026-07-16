package com.storix.domain.domains.notification.service;

import com.storix.domain.domains.notification.adaptor.NotificationSettingAdaptor;
import com.storix.domain.domains.notification.domain.NotificationSetting;
import com.storix.domain.domains.notification.dto.UpdateNotificationSettingCommand;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationSettingService {

    private final NotificationSettingAdaptor notificationSettingAdaptor;

    // 유저 알림 설정 조회
    @Transactional(readOnly = true)
    public NotificationSetting get(Long userId) {
        return notificationSettingAdaptor.getByUserId(userId);
    }

    // 부분 갱신 — 알림 설정 화면 전체 갱신
    @Transactional
    public NotificationSetting update(Long userId, UpdateNotificationSettingCommand cmd) {
        NotificationSetting setting = notificationSettingAdaptor.getByUserId(userId);
        setting.update(
                cmd.likeFeedEnabled(),
                cmd.likeReviewEnabled(),
                cmd.likeCommentEnabled(),
                cmd.commentOnFeedEnabled(),
                cmd.replyOnCommentEnabled(),
                cmd.todayFeedEnabled(),
                cmd.hotTopicRoomEnabled(),
                cmd.operationPolicyEnabled()
        );
        return setting;
    }
}
