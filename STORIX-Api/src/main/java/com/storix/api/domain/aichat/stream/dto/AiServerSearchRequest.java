package com.storix.api.domain.aichat.stream.dto;

import com.storix.api.domain.aichat.stream.AiChatContext;
import com.storix.api.domain.aichat.stream.AiChatQuestion;

public record AiServerSearchRequest(
        Long chatId,
        Long messageId,
        Long userId,
        String question,
        AiServerSearchPolicy policy,
        AiChatContext context
) {
    public static AiServerSearchRequest from(AiChatQuestion question) {
        return new AiServerSearchRequest(
                question.chatId(),
                question.messageId(),
                question.userId(),
                question.question(),
                new AiServerSearchPolicy(
                        question.adultAllowed(),
                        question.worksTypes(),
                        question.excludedWorkIds()
                ),
                question.context()
        );
    }
}
