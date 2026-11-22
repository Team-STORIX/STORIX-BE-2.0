package com.storix.infrastructure.external.aichat.dto;

import java.util.List;

public record AiServerRecommendedWork(
        int rank,
        Long workId,
        String reason,
        List<AiServerMatchedClue> matchedClues
) {
}
