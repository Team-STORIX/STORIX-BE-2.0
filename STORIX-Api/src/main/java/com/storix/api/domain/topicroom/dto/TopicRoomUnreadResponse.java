package com.storix.api.domain.topicroom.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TopicRoomUnreadResponse(

        @Schema(description = "참여 중인 토픽룸에 읽지 않은 메시지가 있는지 여부", example = "true")
        boolean hasUnread
) {
}
