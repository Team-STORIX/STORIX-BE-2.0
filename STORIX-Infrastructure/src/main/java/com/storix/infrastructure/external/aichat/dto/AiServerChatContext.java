package com.storix.infrastructure.external.aichat.dto;

import com.storix.domain.domains.aichat.dto.AiChatMessageInfo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record AiServerChatContext(
        List<AiServerChatTurn> turns,
        List<Long> shownWorkIds,
        List<String> conditions
) {
    public static final int MAX_TURNS = 25;

    // 조건은 AI-06 에서 채움
    public static AiServerChatContext of(List<AiChatMessageInfo> messages, Map<Long, List<Long>> workIdsByMessage) {
        List<AiServerChatTurn> turns = messages.stream()
                .map(message -> AiServerChatTurn.of(
                        message.role(),
                        message.content(),
                        workIdsByMessage.getOrDefault(message.messageId(), List.of())
                ))
                .toList();
        Set<Long> shown = new LinkedHashSet<>();
        turns.forEach(turn -> shown.addAll(turn.workIds()));
        return new AiServerChatContext(
                turns,
                List.copyOf(shown),
                List.of()
        );
    }
}
