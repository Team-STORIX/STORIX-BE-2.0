package com.storix.domain.domains.aichat.dto;

import com.storix.domain.domains.aichat.domain.AiChatAnswerStatus;
import com.storix.domain.domains.aichat.domain.AiChatMessage;
import com.storix.domain.domains.aichat.domain.AiChatMessageRole;
import com.storix.domain.domains.aichat.domain.AiChatMessageType;

import java.time.LocalDateTime;

public record AiChatMessageInfo(
        Long messageId,
        AiChatMessageRole role,
        AiChatMessageType type,
        String content,
        AiChatAnswerStatus status,
        String worksJson,
        LocalDateTime createdAt
) {
    public static AiChatMessageInfo from(AiChatMessage message) {
        return new AiChatMessageInfo(message.getId(), message.getRole(), message.getType(), message.getContent(),
                message.getStatus(), message.getWorksJson(), message.getCreatedAt());
    }
}
