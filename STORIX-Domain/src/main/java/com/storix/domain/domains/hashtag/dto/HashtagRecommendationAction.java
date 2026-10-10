package com.storix.domain.domains.hashtag.dto;

import java.time.LocalDateTime;

public record HashtagRecommendationAction(
        Long worksId,
        double baseScore,
        LocalDateTime createdAt
) {
}
