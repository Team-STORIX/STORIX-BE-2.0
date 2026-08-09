package com.storix.domain.domains.chat.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.storix.domain.domains.chat.domain.ChatMessage;
import com.storix.domain.domains.chat.domain.MessageType;
import jakarta.validation.constraints.NotNull;

// 필드명이 틀린 요청을 조용히 무시하지 않고 실패시킨다
@JsonIgnoreProperties(ignoreUnknown = false)
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
