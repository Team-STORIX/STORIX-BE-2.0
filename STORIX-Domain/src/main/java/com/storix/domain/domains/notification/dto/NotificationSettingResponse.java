package com.storix.domain.domains.notification.dto;

import com.storix.domain.domains.notification.domain.NotificationSetting;

public record NotificationSettingResponse(
        boolean likeFeedEnabled,
        boolean likeReviewEnabled,
        boolean likeCommentEnabled,
        boolean commentOnFeedEnabled,
        boolean replyOnCommentEnabled,
        boolean todayFeedEnabled,
        boolean hotTopicRoomEnabled,
        boolean marketingEnabled
) {
    public static NotificationSettingResponse from(NotificationSetting s) {
        return new NotificationSettingResponse(
                s.isLikeFeedEnabled(),
                s.isLikeReviewEnabled(),
                s.isLikeCommentEnabled(),
                s.isCommentOnFeedEnabled(),
                s.isReplyOnCommentEnabled(),
                s.isTodayFeedEnabled(),
                s.isHotTopicRoomEnabled(),
                s.isMarketingEnabled()
        );
    }
}
