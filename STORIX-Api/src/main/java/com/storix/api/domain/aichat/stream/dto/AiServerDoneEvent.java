package com.storix.api.domain.aichat.stream.dto;

import com.storix.domain.domains.aichat.domain.AiChatAnswerStatus;

import java.util.List;
import java.util.Optional;

public record AiServerDoneEvent(
        String status,
        List<AiServerUsage> usage,
        long elapsedMs
) {
    // ERROR · 모르는 값이면 빈 값. 받는 쪽에서 오류 처리
    public Optional<AiChatAnswerStatus> answerStatus() {
        for (AiChatAnswerStatus candidate : AiChatAnswerStatus.values()) {
            if (candidate != AiChatAnswerStatus.ERROR && candidate.name().equals(status)) {
                return Optional.of(candidate);
            }
        }
        return Optional.empty();
    }
}
