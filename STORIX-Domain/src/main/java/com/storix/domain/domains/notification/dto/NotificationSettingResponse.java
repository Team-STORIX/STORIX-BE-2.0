package com.storix.domain.domains.notification.dto;

import com.storix.domain.domains.notification.domain.NotificationSetting;

// 추후 알림 뎁스 세분화를 위한 테이블 컬럼 유지 (9개 -> 4개 상위 분류)
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
                s.isTodayFeedEnabled() && s.isHotTopicRoomEnabled(),
                s.isMarketingEnabled(),
                s.isOperationPolicyEnabled()
        );
    }
}
