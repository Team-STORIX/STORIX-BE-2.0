package com.storix.api.domain.topicroom.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record TopicRoomNotificationResponse(

        @Schema(description = "토픽룸 채팅 알림 수신 여부", example = "true")
        boolean enabled
) {
}
