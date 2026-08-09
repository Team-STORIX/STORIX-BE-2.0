package com.storix.domain.domains.topicroom.event;

public record TopicRoomChatPushEvent(
        Long roomId,
        Long messageId,
        Long senderId,
        String senderNickname,
        String message
) {
}
