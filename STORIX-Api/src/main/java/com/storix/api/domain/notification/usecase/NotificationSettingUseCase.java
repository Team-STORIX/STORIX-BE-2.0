package com.storix.api.domain.notification.usecase;

import com.storix.common.annotation.UseCase;
import com.storix.common.code.SuccessCode;
import com.storix.common.payload.CustomResponse;
import com.storix.domain.domains.notification.domain.NotificationSetting;
import com.storix.domain.domains.notification.dto.NotificationSettingResponse;
import com.storix.domain.domains.notification.dto.UpdateNotificationSettingRequest;
import com.storix.domain.domains.notification.service.NotificationSettingService;
import lombok.RequiredArgsConstructor;

@UseCase
@RequiredArgsConstructor
public class NotificationSettingUseCase {

    private final NotificationSettingService notificationSettingService;

    // 알림 설정 조회
    public CustomResponse<NotificationSettingResponse> getNotificationSetting(Long userId) {
        NotificationSetting setting = notificationSettingService.get(userId);
        return CustomResponse.onSuccess(SuccessCode.NOTIFICATION_PREFERENCE_LOAD_SUCCESS, NotificationSettingResponse.from(setting));
    }

    // 알림 설정 변경
    public CustomResponse<NotificationSettingResponse> updateNotificationSetting(Long userId, UpdateNotificationSettingRequest request) {
        NotificationSetting updated = notificationSettingService.update(
                userId,
                request.likeFeedEnabled(),
                request.likeReviewEnabled(),
                request.likeCommentEnabled(),
                request.commentOnFeedEnabled(),
                request.replyOnCommentEnabled(),
                request.todayFeedEnabled(),
                request.hotTopicRoomEnabled(),
                request.marketingEnabled()
        );
        return CustomResponse.onSuccess(SuccessCode.NOTIFICATION_PREFERENCE_UPDATE_SUCCESS, NotificationSettingResponse.from(updated));
    }
}
