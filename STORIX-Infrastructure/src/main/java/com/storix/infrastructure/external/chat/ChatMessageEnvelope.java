package com.storix.infrastructure.external.chat;

import com.storix.domain.domains.chat.dto.ChatMessageResponseDto;

public record ChatMessageEnvelope(
        String traceId,
        ChatMessageResponseDto message
) {
    public static ChatMessageEnvelope of(String traceId, ChatMessageResponseDto message) {
        return new ChatMessageEnvelope(traceId, message);
    }
}
