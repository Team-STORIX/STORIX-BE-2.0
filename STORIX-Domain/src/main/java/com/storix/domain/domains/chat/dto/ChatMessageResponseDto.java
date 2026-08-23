package com.storix.domain.domains.chat.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.chat.domain.MessageType;
import com.storix.domain.domains.user.domain.Role;

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
        LocalDateTime createdAt,
        String senderProfileImageUrl,
        Role senderRole
) {
    // 히스토리 조회 JPQL 생성자 표현식용. 프로필은 앱이 멤버 목록으로 채운다
    public ChatMessageResponseDto(
            Long id,
            Long roomId,
            Long senderId,
            String senderName,
            String message,
            MessageType messageType,
            LocalDateTime createdAt,
            Role senderRole
    ) {
        this(id, roomId, senderId, senderName, message, messageType, createdAt, null, senderRole);
    }

    public static ChatMessageResponseDto of(
            ChatMessage chatMessage, String nickname, String senderProfileImageUrl, Role senderRole) {
        return new ChatMessageResponseDto(
                chatMessage.getId(),
                chatMessage.getRoomId(),
                chatMessage.getSenderId(),
                nickname,
                chatMessage.getMessage(),
                chatMessage.getMessageType(),
                chatMessage.getCreatedAt() != null ? chatMessage.getCreatedAt() : LocalDateTime.now(),
                senderProfileImageUrl,
                senderRole
        );
    }
}
