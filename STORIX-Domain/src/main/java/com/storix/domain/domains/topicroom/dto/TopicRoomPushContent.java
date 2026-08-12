package com.storix.domain.domains.topicroom.dto;

import java.util.List;

public record TopicRoomPushContent(
        List<RecentSender> recentSenders,
        String lastMessage
) {
    public RecentSender lastSender() {
        return recentSenders.isEmpty() ? null : recentSenders.get(0);
    }
}
