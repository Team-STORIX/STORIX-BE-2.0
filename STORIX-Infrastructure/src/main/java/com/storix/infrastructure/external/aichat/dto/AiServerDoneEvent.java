package com.storix.infrastructure.external.aichat.dto;

import com.storix.domain.domains.aichat.domain.AiChatAnswerStatus;

import java.util.List;
import java.util.Optional;

public record AiServerDoneEvent(
        String status,
        List<AiServerUsage> usage,
        long elapsedMs
) {
    public Optional<AiChatAnswerStatus> answerStatus() {
        for (AiChatAnswerStatus candidate : AiChatAnswerStatus.values()) {
            if (candidate != AiChatAnswerStatus.ERROR && candidate.name().equals(status)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }
}
