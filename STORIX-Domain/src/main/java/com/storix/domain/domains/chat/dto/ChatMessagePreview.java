package com.storix.domain.domains.chat.dto;

import com.storix.domain.domains.chat.domain.MessageType;

public record ChatMessagePreview(
        Long messageId,
        Long senderId,
        String message,
        MessageType messageType
) {
}
