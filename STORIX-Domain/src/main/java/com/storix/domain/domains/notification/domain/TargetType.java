package com.storix.domain.domains.notification.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TargetType {

    FEED("피드"),           // targetId = feedId,                    parentTargetId = null
    REVIEW("리뷰"),         // targetId = reviewId,                  parentTargetId = null
    COMMENT("댓글"),        // targetId = commentId 또는 parentCommentId, parentTargetId = feedId
    TOPIC_ROOM("토픽룸"),   // targetId = topicRoomId,               parentTargetId = null

    APP_EVENT("자사 이벤트 상세"),   // targetId = 이벤트 id, parentTargetId = null, link = null
    EXTERNAL("앱 외부 URL"),       // targetId = null,      parentTargetId = null, link = 외부 url
    NONE("타겟 없음");             // targetId = null,      parentTargetId = null, link = null

    private final String description;
}
