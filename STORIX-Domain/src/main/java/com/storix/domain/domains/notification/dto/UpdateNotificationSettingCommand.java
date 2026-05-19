package com.storix.domain.domains.notification.dto;

public record UpdateNotificationSettingCommand(
        Boolean likeFeedEnabled,
        Boolean likeReviewEnabled,
        Boolean likeCommentEnabled,
        Boolean commentOnFeedEnabled,
        Boolean replyOnCommentEnabled,
        Boolean todayFeedEnabled,
        Boolean hotTopicRoomEnabled,
        Boolean operationPolicyEnabled
) {
}
