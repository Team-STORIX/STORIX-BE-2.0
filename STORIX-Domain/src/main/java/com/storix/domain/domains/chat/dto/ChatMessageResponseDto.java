package com.storix.domain.domains.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.chat.domain.MessageType;

import java.time.LocalDateTime;

public record ChatMessageResponseDto(
        Long id,
        Long roomId,
        Long senderId,
        String senderName,
        String message,
        MessageType messageType,
        @JsonFormat(
                shape = JsonFormat.Shape.STRING,
                pattern = "yyyy-MM-dd'T'HH:mm:ss",
                timezone = "Asia/Seoul")
        LocalDateTime createdAt
) {
    public static ChatMessageResponseDto of(ChatMessage chatMessage, String nickname) {
        return new ChatMessageResponseDto(
                chatMessage.getId(),
                chatMessage.getRoomId(),
                chatMessage.getSenderId(),
                nickname,
                chatMessage.getMessage(),
                chatMessage.getMessageType(),
                chatMessage.getCreatedAt() != null ? chatMessage.getCreatedAt() : LocalDateTime.now()
        );
    }
}
