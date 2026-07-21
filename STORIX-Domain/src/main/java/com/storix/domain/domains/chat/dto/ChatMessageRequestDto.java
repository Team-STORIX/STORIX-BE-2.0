package com.storix.domain.domains.chat.dto;

import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.chat.domain.MessageType;
import jakarta.validation.constraints.NotNull;

public record ChatMessageRequestDto(
        @NotNull Long roomId,
        @NotNull String message,
        @NotNull MessageType messageType
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
