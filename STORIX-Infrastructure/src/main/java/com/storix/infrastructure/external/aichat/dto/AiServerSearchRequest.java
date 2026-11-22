package com.storix.infrastructure.external.aichat.dto;

public record AiServerSearchRequest(
        Long chatId,
        Long messageId,
        Long userId,
        String question,
        AiServerSearchPolicy policy,
        AiServerChatContext context
) {
}
