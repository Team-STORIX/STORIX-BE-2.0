package com.storix.api.domain.aichat.stream;

import com.storix.domain.domains.aichat.dto.AiChatMessageInfo;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record AiChatContext(
        List<AiChatTurn> turns,
        List<Long> shownWorkIds,
        List<String> conditions
) {

    public static final int MAX_TURNS = 25;

    // 보여 준 작품은 넘기는 대화 안에서만. 조건(완결작만 등)은 AI-06 에서
    public static AiChatContext of(List<AiChatMessageInfo> messages, Map<Long, List<AiChatRawWork>> worksByMessage) {
        List<AiChatTurn> turns = messages.stream()
                .map(message -> AiChatTurn.of(message.role(), message.content(),
                        worksByMessage.getOrDefault(message.messageId(), List.of())))
                .toList();
        Set<Long> shown = new LinkedHashSet<>();
        turns.forEach(turn -> shown.addAll(turn.workIds()));
        return new AiChatContext(
                turns,
                List.copyOf(shown),
                List.of()
        );
    }
}
