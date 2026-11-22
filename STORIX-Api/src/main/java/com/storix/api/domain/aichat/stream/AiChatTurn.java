package com.storix.api.domain.aichat.stream;

import com.storix.domain.domains.aichat.domain.AiChatMessageRole;

import java.util.List;

public record AiChatTurn(
        String role,
        String text,
        List<Long> workIds
) {
    public static AiChatTurn of(AiChatMessageRole role, String text, List<AiChatRawWork> works) {
        return new AiChatTurn(
                role == AiChatMessageRole.USER ? "user" : "assistant",
                text,
                works.stream().map(AiChatRawWork::workId).toList()
        );
    }
}
