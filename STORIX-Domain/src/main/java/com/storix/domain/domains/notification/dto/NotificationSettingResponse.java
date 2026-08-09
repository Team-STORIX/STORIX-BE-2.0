package com.storix.domain.domains.notification.dto;

import com.storix.domain.domains.notification.domain.NotificationSetting;

public record NotificationSettingResponse(
        boolean myActivityEnabled,
        boolean contentCommunityEnabled,
        boolean eventBenefitEnabled,
        boolean operationPolicyEnabled
) {
    public static NotificationSettingResponse from(NotificationSetting s) {
        return new NotificationSettingResponse(
                s.isLikeFeedEnabled() && s.isLikeReviewEnabled() && s.isLikeCommentEnabled()
                        && s.isCommentOnFeedEnabled() && s.isReplyOnCommentEnabled(),
                s.isTodayFeedEnabled() && s.isHotTopicRoomEnabled() && s.isTopicRoomChatEnabled(),
                s.isMarketingEnabled(),
                s.isOperationPolicyEnabled()
        );
    }
}
