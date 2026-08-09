package com.storix.api.domain.topicroom.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record TopicRoomNotificationRequest(

        @Schema(description = "토픽룸 채팅 알림 수신 여부 (true: ON, false: OFF)", example = "true")
        @NotNull(message = "알림 설정 값을 보내주세요.")
        Boolean enabled
) {
}
