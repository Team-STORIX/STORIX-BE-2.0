package com.storix.api.domain.aichat.controller.dto;

import java.util.List;

public record AiChatRecommendedWork(
        int rank,
        Long workId,
        String title,
        String thumbnailUrl,
        String platform,
        String genre,
        String reason,
        List<AiChatMatchedClue> matchedClues
) {
}
