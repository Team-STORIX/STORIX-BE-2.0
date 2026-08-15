package com.storix.domain.domains.topicroom.dto;

import java.util.List;

public record TopicRoomChatPushTarget(
        Long userId,
        List<String> tokens,
        int batchMessageCount,
        int badgeCount
) {
}
