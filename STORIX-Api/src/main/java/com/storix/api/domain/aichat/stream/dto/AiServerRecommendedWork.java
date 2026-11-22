package com.storix.api.domain.aichat.stream.dto;

import com.storix.api.domain.aichat.controller.dto.AiChatMatchedClue;

import java.util.List;

public record AiServerRecommendedWork(
        int rank,
        Long workId,
        String reason,
        List<AiChatMatchedClue> matchedClues
) {
}
