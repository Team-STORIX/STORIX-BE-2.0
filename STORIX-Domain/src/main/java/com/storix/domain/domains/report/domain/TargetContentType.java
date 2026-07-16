package com.storix.domain.domains.report.domain;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "대상 콘텐츠 타입. FEED=게시글, FEED_REPLY=댓글, REVIEW=리뷰, TOPIC_ROOM=토픽룸 유저 신고, CHAT=채팅 메시지")
public enum TargetContentType {
    FEED,
    FEED_REPLY,
    REVIEW,
    TOPIC_ROOM,
    CHAT
}
