package com.storix.domain.domains.topicroom.dto;

public record PendingChatPush(
        Long lastMessageId,
        Long lastSenderId,
        String lastSenderNickname,
        String lastMessage
) {
}
