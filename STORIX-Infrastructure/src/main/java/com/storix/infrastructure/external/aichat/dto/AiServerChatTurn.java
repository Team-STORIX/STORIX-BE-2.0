package com.storix.infrastructure.external.aichat.dto;

import com.storix.domain.domains.aichat.domain.AiChatMessageRole;

import java.util.List;

public record AiServerChatTurn(
        String role,
        String text,
        List<Long> workIds
) {
    public static AiServerChatTurn of(AiChatMessageRole role, String text, List<Long> workIds) {
        return new AiServerChatTurn(
                role == AiChatMessageRole.USER ? "user" : "assistant",
                text,
                workIds
        );
    }
}
