package com.storix.domain.domains.topicroom.dto;

public record RecentSenderRow(
        Long receiverId,
        Long senderId,
        Long lastMessageId
) {
}
