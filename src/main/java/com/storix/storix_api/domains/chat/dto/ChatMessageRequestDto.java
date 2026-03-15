package com.storix.storix_api.domains.chat.dto;

import com.storix.storix_api.domains.chat.domain.ChatMessage;
import com.storix.storix_api.domains.chat.domain.MessageType;

public record ChatMessageRequestDto(
        Long roomId,
        String message,
        MessageType messageType
) {
    public ChatMessage toEntity(Long senderId) {
        return ChatMessage.builder()
                .roomId(this.roomId)
                .senderId(senderId)
                .message(this.message)
                .messageType(this.messageType)
                .build();
    }
}
