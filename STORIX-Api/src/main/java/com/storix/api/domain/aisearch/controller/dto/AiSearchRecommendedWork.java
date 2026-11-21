package com.storix.api.domain.aisearch.controller.dto;

import java.util.List;

public record AiSearchRecommendedWork(
        int rank,
        Long workId,
        String title,
        String thumbnailUrl,
        String platform,
        String reason,
        List<AiSearchMatchedClue> matchedClues
) {
}
