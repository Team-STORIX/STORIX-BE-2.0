package com.storix.domain.domains.notification.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record UpdateNotificationSettingRequest(

        /* ─────────── 서비스 알림 ─────────── */
        @Schema(description = "내 피드에 좋아요", example = "true")
        Boolean likeFeedEnabled,

        @Schema(description = "내 리뷰에 좋아요", example = "true")
        Boolean likeReviewEnabled,

        @Schema(description = "내 피드 댓글에 좋아요", example = "true")
        Boolean likeCommentEnabled,

        @Schema(description = "내 피드에 댓글", example = "true")
        Boolean commentOnFeedEnabled,

        @Schema(description = "내 댓글에 답댓글", example = "true")
        Boolean replyOnCommentEnabled,

        @Schema(description = "내 피드가 오늘의 피드로 선정", example = "true")
        Boolean todayFeedEnabled,

        @Schema(description = "내가 참여한 토픽룸이 HOT으로 선정", example = "true")
        Boolean hotTopicRoomEnabled,

        /* ─────────── 마케팅/광고 ─────────── */
        @Schema(description = "운영자 발송 이벤트/광고", example = "false")
        Boolean marketingEnabled
) {
}
